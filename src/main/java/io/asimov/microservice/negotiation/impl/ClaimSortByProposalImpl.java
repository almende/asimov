package io.asimov.microservice.negotiation.impl;

import io.asimov.messaging.ASIMOVMessageID;
import io.asimov.microservice.negotiation.ClaimSortByProposal;
import io.asimov.microservice.negotiation.ConversionCallback;
import io.asimov.microservice.negotiation.messages.Claim;
import io.asimov.microservice.negotiation.messages.Proposal;
import io.asimov.microservice.negotiation.messages.ProposalRequest;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.interact.ReceivingCapability;
import io.coala.capability.interact.SendingCapability;
import io.coala.log.InjectLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.google.inject.Inject;

/**
 * {@link ClaimSortByProposalImpl}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:suki@almende.org">suki</a>
 * 
 */
public class ClaimSortByProposalImpl extends BasicCapability implements
		ClaimSortByProposal
{

	@InjectLogger
	private Logger LOG;

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private Observer<Proposal> messageObserver = null;

	/**
	 * The list of requested proposals to sort the claims with.
	 */
	private List<ProposalRequest> proposalRequests;

	/**
	 * The list of currently sort-able claims.
	 */
	private Map<ASIMOVMessageID, Claim> candidateClaims;

	/**
	 * The list of received proposals to sort the claims with.
	 */
	private List<Proposal> proposals;

	private Comparator<Proposal> proposalComparator;

	private Subject<Claim, Claim> result;
	
	private class CachedSortJob {
		
		public CachedSortJob(final Iterable<Claim> claims,
				final ConversionCallback scoreQueryAdapter,
				final Comparator<Proposal> proposalComparator,
				final Subject<Claim,Claim> result){
			this.claims = claims;
			this.scoreQueryAdapter = scoreQueryAdapter;
			this.proposalComparator = proposalComparator;
			this.result = result;
		}
		
		final Iterable<Claim> claims;
		final ConversionCallback scoreQueryAdapter;
		final Comparator<Proposal> proposalComparator;
		final Subject<Claim,Claim> result;
	}
	
	private LinkedBlockingQueue<CachedSortJob> queue = new LinkedBlockingQueue<CachedSortJob>();

	/**
	 * {@link ClaimSortByProposalImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected ClaimSortByProposalImpl(Binder binder)
	{
		super(binder);
	}

	@Override
	public void activate()
	{
		if (this.messageObserver == null)
		{
			this.candidateClaims = new HashMap<ASIMOVMessageID, Claim>();
			this.proposalRequests = new ArrayList<ProposalRequest>();
			this.proposals = new ArrayList<Proposal>();
			this.messageObserver = new Observer<Proposal>()
			{

				@Override
				public void onNext(Proposal proposal)
				{
					handle(proposal);
				}

				@Override
				public void onError(Throwable throwable)
				{
					LOG.error(
							"Failed to sort claims because of the following exception: "
									+ throwable.getMessage(), throwable);
				}

				@Override
				public void onCompleted()
				{
					// nothing special here
				}
			};
			getBinder().inject(ReceivingCapability.class).getIncoming()
					.ofType(Proposal.class).subscribe(messageObserver);
		} else
		{
			LOG.warn("Claim sort by proposal service was already intialized.");
		}
	}

	/** @see eu.a4ee.negotiation.ClaimSortByProposal#handle(eu.a4ee.negotiation.messages.Proposal) */
	@Override
	public void handle(Proposal p)
	{
		if (this.candidateClaims.containsKey(p.getReplyToId()))
		{
			this.proposals.add(p);
			//LOG.warn("Got score: "+p.getScore()+" from " + this.proposals.size() + " out of "
			//		+ this.proposalRequests.size() + " proposals...");
			if (this.proposals.size() == this.proposalRequests.size())
			{
				//LOG.warn("Received all proposals, finding best fit now!");
				Collections.sort(this.proposals, this.proposalComparator);
				synchronized (this)
				{
					//boolean printedBestFit = false;
					for (Proposal proposal : this.proposals){
//						if (!printedBestFit) {
//							printedBestFit = true;
//							LOG.warn("Best fit = "+proposal.getScore());
//						}
						result.onNext(this.candidateClaims.get(proposal
								.getReplyToId()));
					}
					result.onCompleted();
					reset();
				}
			}
		} else
		{
			throw new IllegalStateException(
					"Got a reply for an unknown candidate claim, unable to sort.");
		}
	}

	private void reset()
	{
		this.candidateClaims = new HashMap<ASIMOVMessageID, Claim>();
		this.proposalRequests = new ArrayList<ProposalRequest>();
		this.proposals = new ArrayList<Proposal>();
		this.result = null;
		this.proposalComparator = null;
		CachedSortJob job = queue.poll();
		if (job != null) {
			try
			{
				result = job.result;
				sort(job.claims, job.scoreQueryAdapter, job.proposalComparator);
			} catch (Exception e)
			{
				LOG.error(e.getMessage(), e); 
			}
		}
	}
	
	
	/**
	 * @throws Exception
	 * @see eu.a4ee.negotiation.ClaimSortByProposal#sort(java.lang.Iterable,
	 *      eu.a4ee.negotiation.ConversionCallback, java.util.Comparator)
	 */
	@Override
	public synchronized Observable<Claim> sort(Iterable<Claim> claims,
			ConversionCallback scoreQueryAdapter,
			Comparator<Proposal> proposalComparator) throws Exception
	{
		Subject<Claim,Claim> result = ReplaySubject.create();
		if (!this.proposals.isEmpty() || !this.proposals.isEmpty()
				|| !this.candidateClaims.isEmpty()) {
			final Subject<Claim,Claim> r = ReplaySubject.create();
			queue.add(new CachedSortJob(claims, scoreQueryAdapter, proposalComparator, r));
//			throw new IllegalStateException(
//					"Already sorting another set of claims!");
			return r.asObservable();
		}
		else if (this.result == null) {
			this.result = result;
		}
		
		this.proposalComparator = proposalComparator;
		for (Claim c : claims)
		{
			ProposalRequest pr = new ProposalRequest(c.getID().getTime(),
					c.getSenderID(), c.getReceiverID());
			pr.setClaim(c);
			pr.setQuery(scoreQueryAdapter.convert(c.getQuery()));
			this.proposalRequests.add(pr);
			this.candidateClaims.put(pr.getID(), c);
		}
		for (ProposalRequest pr : this.proposalRequests)
			getBinder().inject(SendingCapability.class).send(pr);

		return result.asObservable();
	}

}
