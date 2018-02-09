package common.implementation;

public abstract class Constants
{
	public static enum QuestionTypes {
		__NULL__,
		SINGLE_OPTION,
		MULTIPLE_OPTION,
		SLIDER,
		AREA
	}

	public static <T extends Enum<T>> T getEnum(T v[], int ordinal) {
		for (T t : v) {
			if (t.ordinal() == ordinal) {
				return t;
			}
		}
		return v[0];
	}

	public static <T extends Enum<T>> T getEnum(T v[], String ordinalStr) throws NumberFormatException {
		return getEnum(v, Integer.parseInt(ordinalStr));
	}

	public static <T extends Enum<T>> boolean equal(T lhs, T rhs) {
		return lhs.compareTo(rhs) == 0;
	}
}
