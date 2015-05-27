package de.zmt.util;

public abstract class ParamUtil {
    public static <T extends Enum<T>> String[] obtainEnumDomain(Class<T> enumType) {
	T[] enumConstants = enumType.getEnumConstants();
	String[] enumNames = new String[enumConstants.length];

	for (int i = 0; i < enumConstants.length; i++) {
	    enumNames[i] = enumConstants[i].name();
	}

	return enumNames;
    }

}
