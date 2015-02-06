/* $Id: SLConvertible.java 1048 2014-09-01 09:53:05Z krevelen $
 * $URL: https://redmine.almende.com/svn/a4eesim/trunk/adapt4ee-model/src/main/java/eu/a4ee/model/jsa/SLConvertible.java $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2010-2013 Almende B.V. 
 */
package io.asimov.model.sl;

import jade.semantics.interpreter.SemanticCapabilities;
import jade.semantics.lang.sl.grammar.ActionExpression;
import jade.semantics.lang.sl.grammar.ActionExpressionNode;
import jade.semantics.lang.sl.grammar.Constant;
import jade.semantics.lang.sl.grammar.Formula;
import jade.semantics.lang.sl.grammar.IdentifyingExpression;
import jade.semantics.lang.sl.grammar.ListOfNodes;
import jade.semantics.lang.sl.grammar.MetaTermReferenceNode;
import jade.semantics.lang.sl.grammar.NotNode;
import jade.semantics.lang.sl.grammar.OrNode;
import jade.semantics.lang.sl.grammar.Term;
import jade.semantics.lang.sl.tools.SL;
import jade.util.leap.ArrayList;

import java.util.Iterator;

/**
 * {@link SLConvertible} tags POJOs that have a JSA representation, {@link #toSL()}
 * 
 * @date $Date: 2014-09-01 11:53:05 +0200 (ma, 01 sep 2014) $
 * @version $Revision: 1048 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 * 
 */
public interface SLConvertible<T extends SLConvertible<?>> extends io.coala.jsa.sl.SLConvertible<T>
{

	/** */
	String AT = "@";

	
	public static final String sASIMOV_PROPERTY = "PROPERTY";
	
	public static final String sASIMOV_KEY = "KEY";
	
	public static final String sASIMOV_VALUE = "VALUE";
	
		public static final Formula ASIMOV_PROPERTY_SET_FORMULA = SL.formula(
			String.format("(ASIMOV_PROPERTY_SET ??%s ??%s ??%s)"
			,sASIMOV_PROPERTY, sASIMOV_KEY, sASIMOV_VALUE)
	);
	

	
	final public static class FOLToSLHelper {
		
		final static String ifx = "_";
		
		@Deprecated
		final public static void appendRulesToCapabilities(@SuppressWarnings("rawtypes") java.util.ArrayList rules, SemanticCapabilities capabilities) {
		}
		
		@Deprecated
		final public static void appendRulesToCapabilities(ArrayList rules, SemanticCapabilities capabilities) {
			Iterator<?> ruleIterator = rules.iterator();
			while (ruleIterator.hasNext()) {
				capabilities.interpret((Formula) ruleIterator.next());
				
			}
		}
		
		/*
		 * convenience method to overcome the JSA bug that prevents creation of implies Nodes with constructor
		 */
		
		final public static Formula generateImpliesNode(Formula leftFormula, Formula rightFormula) {
			return SL.formula("(implies "+leftFormula.toString()+" "+rightFormula.toString()+")");
		}
		
		final public static String pfx(String inputMetaReferenceName, String pfx) {
			return inputMetaReferenceName+ifx+pfx;
		}
		
		final public static Formula pfx(Formula inputFormula, String pfx) {
			Formula outputFormula = (Formula)inputFormula.getClone();
		    Formula body = outputFormula.getSimplifiedFormula();
			
			ListOfNodes metaRefs = new ListOfNodes();
			body.childrenOfKind(SL.META_REFERENCE_CLASSES, metaRefs);
			ListOfNodes replacedMetaRefs = new ListOfNodes();
			for (int i=0; i<metaRefs.size(); i++) {
				if (!replacedMetaRefs.contains(metaRefs.get(i))) {
					SL.setMetaReferenceName(metaRefs.get(i),
							SL.getMetaReferenceName(metaRefs.get(i))+ifx+pfx);// + SL.getMetaReferenceName(metaRefs.get(i)));
					replacedMetaRefs.add(metaRefs.get(i));
				}
			}
			return outputFormula;
		}
		
		final public static Formula replaceMetaReference(Formula inputFormula, String find, String replace) {
			Formula outputFormula = (Formula)inputFormula.getClone();
		    Formula body = outputFormula.getSimplifiedFormula();
			
			ListOfNodes metaRefs = new ListOfNodes();
			body.childrenOfKind(SL.META_REFERENCE_CLASSES, metaRefs);
			ListOfNodes replacedMetaRefs = new ListOfNodes();
			for (int i=0; i<metaRefs.size(); i++) {
				if (!replacedMetaRefs.contains(metaRefs.get(i)) && SL.getMetaReferenceName(metaRefs.get(i)).equals(find)) {
					SL.setMetaReferenceName(metaRefs.get(i),
							replace);// + SL.getMetaReferenceName(metaRefs.get(i)));
					replacedMetaRefs.add(metaRefs.get(i));
				}
			}
			return outputFormula;
		}
		
	}
	
	
}
