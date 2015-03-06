package io.asimov.model.sl;


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
	
		public static final ASIMOVFormula ASIMOV_PROPERTY_SET_FORMULA = new ASIMOVFormula()
		.withName("ASIMOV_PROPERTY_SET")
			.instantiate(sASIMOV_PROPERTY,null)
			.instantiate(sASIMOV_KEY, null)
			.instantiate(sASIMOV_VALUE, null);

//	
//	final public static class FOLToSLHelper {
//		
//		final static String ifx = "_";
//		
//		@Deprecated
//		final public static void appendRulesToCapabilities(@SuppressWarnings("rawtypes") java.util.ArrayList rules, SemanticCapabilities capabilities) {
//		}
//		
//		@Deprecated
//		final public static void appendRulesToCapabilities(ArrayList rules, SemanticCapabilities capabilities) {
//			Iterator<?> ruleIterator = rules.iterator();
//			while (ruleIterator.hasNext()) {
//				capabilities.interpret((Formula) ruleIterator.next());
//				
//			}
//		}
//		
//		/*
//		 * convenience method to overcome the JSA bug that prevents creation of implies Nodes with constructor
//		 */
//		
//		final public static Formula generateImpliesNode(Formula leftFormula, Formula rightFormula) {
//			return SL.formula("(implies "+leftFormula.toString()+" "+rightFormula.toString()+")");
//		}
		
//		final public static String pfx(String inputMetaReferenceName, String pfx) {
//			return inputMetaReferenceName+ifx+pfx;
//		}
////		
//		final public static Formula pfx(Formula inputFormula, String pfx) {
//			Formula outputFormula = (Formula)inputFormula.getClone();
//		    Formula body = outputFormula.getSimplifiedFormula();
//			
//			ListOfNodes metaRefs = new ListOfNodes();
//			body.childrenOfKind(SL.META_REFERENCE_CLASSES, metaRefs);
//			ListOfNodes replacedMetaRefs = new ListOfNodes();
//			for (int i=0; i<metaRefs.size(); i++) {
//				if (!replacedMetaRefs.contains(metaRefs.get(i))) {
//					SL.setMetaReferenceName(metaRefs.get(i),
//							SL.getMetaReferenceName(metaRefs.get(i))+ifx+pfx);// + SL.getMetaReferenceName(metaRefs.get(i)));
//					replacedMetaRefs.add(metaRefs.get(i));
//				}
//			}
//			return outputFormula;
//		}
//		
//		final public static Formula replaceMetaReference(Formula inputFormula, String find, String replace) {
//			Formula outputFormula = (Formula)inputFormula.getClone();
//		    Formula body = outputFormula.getSimplifiedFormula();
//			
//			ListOfNodes metaRefs = new ListOfNodes();
//			body.childrenOfKind(SL.META_REFERENCE_CLASSES, metaRefs);
//			ListOfNodes replacedMetaRefs = new ListOfNodes();
//			for (int i=0; i<metaRefs.size(); i++) {
//				if (!replacedMetaRefs.contains(metaRefs.get(i)) && SL.getMetaReferenceName(metaRefs.get(i)).equals(find)) {
//					SL.setMetaReferenceName(metaRefs.get(i),
//							replace);// + SL.getMetaReferenceName(metaRefs.get(i)));
//					replacedMetaRefs.add(metaRefs.get(i));
//				}
//			}
//			return outputFormula;
//		}
//		
//	}
	
	
}
