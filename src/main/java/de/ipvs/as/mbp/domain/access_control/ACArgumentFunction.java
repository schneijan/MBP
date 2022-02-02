package de.ipvs.as.mbp.domain.access_control;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Enumeration for different functions that can be used to compare two arguments.
 */
public enum ACArgumentFunction {
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * two arguments are equal.
	 */
	EQUALS("=", compareResult -> compareResult == 0, String::equalsIgnoreCase),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * two arguments are not equal.
	 */
	NOT_EQUALS("!=", compareResult -> compareResult != 0, (l, r) -> !l.equalsIgnoreCase(r)),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is less than the second argument.
	 */
	LESS_THAN("<", compareResult -> compareResult < 0, null),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is less than or equal to the second argument.
	 */
	LESS_THAN_OR_EQUAL_TO("<=", compareResult -> compareResult <= 0, null),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is greater than the second argument.
	 */
	GREATER_THAN(">", compareResult -> compareResult > 0, null),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is greater than or equal to the second argument.
	 */
	GREATER_THAN_OR_EQUAL_TO(">=", compareResult -> compareResult >= 0, null),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument starts with the second argument.
	 */
	STARTS_WITH("starts with", null, (l, r) -> l.toLowerCase().startsWith(r.toLowerCase())),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument does not start with the second argument.
	 */
	NOT_STARTS_WITH("does not start with", null, (l, r) -> !l.toLowerCase().startsWith(r.toLowerCase())),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument contains the second argument.
	 */
	CONTAINS("contains", null, (l, r) -> l.toLowerCase().contains(r.toLowerCase())),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument does not contain the second argument.
	 */
	NOT_CONTAINS("does not contain", null, (l, r) -> l.toLowerCase().contains(r.toLowerCase())),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument ends with the second argument.
	 */
	ENDS_WITH("ends with", null, (l, r) -> l.toLowerCase().endsWith(r.toLowerCase())),

	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument does not end with the second argument.
	 */
	NOT_ENDS_WITH("does not end with", null, (l, r) -> !l.toLowerCase().endsWith(r.toLowerCase()));
	
	// - - -
	
	/**
	 * A human readable description.
	 */
	private final String humanReadableDescription;
	
	/**
	 * The predicate used to evaluate the result of the call to {@link Comparable#compareTo(Object)}.
	 */
	private final Predicate<Integer> evaluationPredicate;
	
	/**
	 * The function used to evaluate functions on strings.
	 */
	private final BiFunction<String, String, Boolean> evaluationFunction;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param humanReadableDescription the human readable description.
	 * @param evaluationPredicate The {@link Predicate} used to evaluate
	 * 		  the result of the call to {@link Comparable#compareTo(Object)}.
	 * @param evaluationFunction the function used to evaluate functions on strings.
	 */
	private ACArgumentFunction(String humanReadableDescription, Predicate<Integer> evaluationPredicate, BiFunction<String, String, Boolean> evaluationFunction) {
		this.humanReadableDescription = humanReadableDescription;
		this.evaluationPredicate = evaluationPredicate;
		this.evaluationFunction = evaluationFunction;
	}
	
	// - - -
	
	public String getHumanReadableDescription() {
		return humanReadableDescription;
	}
	
	public Predicate<Integer> getEvaluationPredicate() {
		return evaluationPredicate;
	}
	
	public BiFunction<String, String, Boolean> getEvaluationFunction() {
		return evaluationFunction;
	}
	
	// - - -
	
	/**
	 * Convenience function to directly apply this argument function to
	 * the given arguments.
	 * 
	 * @param left the left side argument.
	 * @param right the right side argument.
	 * @return {@code true} if and only if the predicate associated with this
	 * 		   argument function holds; {@code false} otherwise.
	 */
	public <T extends Comparable<T>> boolean apply(T left, T right) {
		if (evaluationPredicate != null) {			
			return applyPredicate(left.compareTo(right));
		} else {
			return applyFunction(left.toString(), right.toString());
		}
	}
	
	public boolean applyPredicate(int compareResult) {
		return evaluationPredicate.test(compareResult);
	}
	
	public boolean applyFunction(String left, String right) {
		return evaluationFunction.apply(left, right);
	}
	
	// - - -
	
	public static ACArgumentFunction basedOn(String jqbOperator) {
		switch (jqbOperator) {
			case "equal": return EQUALS;
			case "not_equal": return NOT_EQUALS;
			case "less": return LESS_THAN;
			case "less_or_equal": return LESS_THAN_OR_EQUAL_TO;
			case "greater": return GREATER_THAN;
			case "greater_or_equal": return GREATER_THAN_OR_EQUAL_TO;
			case "begins_with": return STARTS_WITH;
			case "not_begins_with": return NOT_STARTS_WITH;
			case "contains": return CONTAINS;
			case "not_contains": return NOT_CONTAINS;
			case "ends_with": return ENDS_WITH;
			case "not_ends_with": return NOT_ENDS_WITH;
			default: return null;
		}
	}
	
}
