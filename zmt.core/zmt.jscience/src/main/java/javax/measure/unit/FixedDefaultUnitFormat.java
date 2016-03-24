package javax.measure.unit;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.measure.quantity.Quantity;

/**
 * Fixed version of the default unit format in {@link UnitFormat}.
 * 
 * <pre>
 * CHANGES
 *     {@link #parseProductUnit(CharSequence, ParsePosition)}
 *     added cases for INTEGER and FLOAT to handle units like 1/day
 * </pre>
 */
/*
 * This weird wrapper-like construct is needed because the default UnitFormat
 * cannot be set manually and is accessed statically to set all the labels and
 * aliases for the default units.
 */
public class FixedDefaultUnitFormat extends UnitFormat {
    @SuppressWarnings("unchecked")
    @Override
    public Unit<? extends Quantity> parseProductUnit(CharSequence csq, ParsePosition pos) throws ParseException {
	Unit<?> result = Unit.ONE;
	int token = nextToken(csq, pos);
	switch (token) {
	case IDENTIFIER:
	    result = parseSingleUnit(csq, pos);
	    break;
	case OPEN_PAREN:
	    pos.setIndex(pos.getIndex() + 1);
	    result = parseProductUnit(csq, pos);
	    token = nextToken(csq, pos);
	    check(token == CLOSE_PAREN, "')' expected", csq, pos.getIndex());
	    pos.setIndex(pos.getIndex() + 1);
	    break;
	}
	token = nextToken(csq, pos);
	while (true) {
	    switch (token) {
	    case EXPONENT:
		Exponent e = readExponent(csq, pos);
		if (e.pow != 1) {
		    result = result.pow(e.pow);
		}
		if (e.root != 1) {
		    result = result.root(e.root);
		}
		break;
	    case MULTIPLY:
		pos.setIndex(pos.getIndex() + 1);
		token = nextToken(csq, pos);
		if (token == INTEGER) {
		    long n = readLong(csq, pos);
		    if (n != 1) {
			result = result.times(n);
		    }
		} else if (token == FLOAT) {
		    double d = readDouble(csq, pos);
		    if (d != 1.0) {
			result = result.times(d);
		    }
		} else {
		    result = result.times(parseProductUnit(csq, pos));
		}
		break;
	    case DIVIDE:
		pos.setIndex(pos.getIndex() + 1);
		token = nextToken(csq, pos);
		if (token == INTEGER) {
		    long n = readLong(csq, pos);
		    if (n != 1) {
			result = result.divide(n);
		    }
		} else if (token == FLOAT) {
		    double d = readDouble(csq, pos);
		    if (d != 1.0) {
			result = result.divide(d);
		    }
		} else {
		    result = result.divide(parseProductUnit(csq, pos));
		}
		break;
	    case PLUS:
		pos.setIndex(pos.getIndex() + 1);
		token = nextToken(csq, pos);
		if (token == INTEGER) {
		    long n = readLong(csq, pos);
		    if (n != 1) {
			result = result.plus(n);
		    }
		} else if (token == FLOAT) {
		    double d = readDouble(csq, pos);
		    if (d != 1.0) {
			result = result.plus(d);
		    }
		} else {
		    throw new ParseException("not a number", pos.getIndex());
		}
		break;
	    case INTEGER:
		long n = readLong(csq, pos);
		if (n != 1) {
		    result = result.plus(n);
		}
		break;
	    case FLOAT:
		double d = readDouble(csq, pos);
		if (d != 1.0) {
		    result = result.plus(d);
		}
	    case EOF:
	    case CLOSE_PAREN:
		return result;
	    default:
		throw new ParseException("unexpected token " + token, pos.getIndex());
	    }
	    token = nextToken(csq, pos);
	}
    }

    private static final int EOF = 0;
    private static final int IDENTIFIER = 1;
    private static final int OPEN_PAREN = 2;
    private static final int CLOSE_PAREN = 3;
    private static final int EXPONENT = 4;
    private static final int MULTIPLY = 5;
    private static final int DIVIDE = 6;
    private static final int PLUS = 7;
    private static final int INTEGER = 8;
    private static final int FLOAT = 9;

    private static int nextToken(CharSequence csq, ParsePosition pos) {
	final int length = csq.length();
	while (pos.getIndex() < length) {
	    char c = csq.charAt(pos.getIndex());
	    if (UnitFormat.DefaultFormat.isUnitIdentifierPart(c)) {
		return IDENTIFIER;
	    } else if (c == '(') {
		return OPEN_PAREN;
	    } else if (c == ')') {
		return CLOSE_PAREN;
	    } else if ((c == '^') || (c == '¹') || (c == '²') || (c == '³')) {
		return EXPONENT;
	    } else if (c == '*') {
		char c2 = csq.charAt(pos.getIndex() + 1);
		if (c2 == '*') {
		    return EXPONENT;
		} else {
		    return MULTIPLY;
		}
	    } else if (c == '·') {
		return MULTIPLY;
	    } else if (c == '/') {
		return DIVIDE;
	    } else if (c == '+') {
		return PLUS;
	    } else if ((c == '-') || Character.isDigit(c)) {
		int index = pos.getIndex() + 1;
		while ((index < length) && (Character.isDigit(c) || (c == '-') || (c == '.') || (c == 'E'))) {
		    c = csq.charAt(index++);
		    if (c == '.') {
			return FLOAT;
		    }
		}
		return INTEGER;
	    }
	    pos.setIndex(pos.getIndex() + 1);
	}
	return EOF;
    }

    private static void check(boolean expr, String message, CharSequence csq, int index) throws ParseException {
	if (!expr) {
	    throw new ParseException(message + " (in " + csq + " at index " + index + ")", index);
	}
    }

    private static Exponent readExponent(CharSequence csq, ParsePosition pos) {
	char c = csq.charAt(pos.getIndex());
	if (c == '^') {
	    pos.setIndex(pos.getIndex() + 1);
	} else if (c == '*') {
	    pos.setIndex(pos.getIndex() + 2);
	}
	final int length = csq.length();
	int pow = 0;
	boolean isPowNegative = false;
	int root = 0;
	boolean isRootNegative = false;
	boolean isRoot = false;
	while (pos.getIndex() < length) {
	    c = csq.charAt(pos.getIndex());
	    if (c == '¹') {
		if (isRoot) {
		    root = root * 10 + 1;
		} else {
		    pow = pow * 10 + 1;
		}
	    } else if (c == '²') {
		if (isRoot) {
		    root = root * 10 + 2;
		} else {
		    pow = pow * 10 + 2;
		}
	    } else if (c == '³') {
		if (isRoot) {
		    root = root * 10 + 3;
		} else {
		    pow = pow * 10 + 3;
		}
	    } else if (c == '-') {
		if (isRoot) {
		    isRootNegative = true;
		} else {
		    isPowNegative = true;
		}
	    } else if ((c >= '0') && (c <= '9')) {
		if (isRoot) {
		    root = root * 10 + (c - '0');
		} else {
		    pow = pow * 10 + (c - '0');
		}
	    } else if (c == ':') {
		isRoot = true;
	    } else {
		break;
	    }
	    pos.setIndex(pos.getIndex() + 1);
	}
	if (pow == 0) {
	    pow = 1;
	}
	if (root == 0) {
	    root = 1;
	}
	return new Exponent(isPowNegative ? -pow : pow, isRootNegative ? -root : root);
    }

    private static long readLong(CharSequence csq, ParsePosition pos) {
	final int length = csq.length();
	int result = 0;
	boolean isNegative = false;
	while (pos.getIndex() < length) {
	    char c = csq.charAt(pos.getIndex());
	    if (c == '-') {
		isNegative = true;
	    } else if ((c >= '0') && (c <= '9')) {
		result = result * 10 + (c - '0');
	    } else {
		break;
	    }
	    pos.setIndex(pos.getIndex() + 1);
	}
	return isNegative ? -result : result;
    }

    private static double readDouble(CharSequence csq, ParsePosition pos) {
	final int length = csq.length();
	int start = pos.getIndex();
	int end = start + 1;
	while (end < length) {
	    if ("012356789+-.E".indexOf(csq.charAt(end)) < 0) {
		break;
	    }
	    end += 1;
	}
	pos.setIndex(end + 1);
	return Double.parseDouble(csq.subSequence(start, end).toString());
    }

    // //////////////////////////
    // Formatting.

    private static final long serialVersionUID = 1L;

    /**
     * This class represents an exponent with both a power (numerator) and a
     * root (denominator).
     */
    private static class Exponent {
	public final int pow;
	public final int root;

	public Exponent(int pow, int root) {
	    this.pow = pow;
	    this.root = root;
	}
    }

    @Override
    public Appendable format(Unit<?> unit, Appendable appendable) throws IOException {
	return UnitFormat.getInstance().format(unit, appendable);
    }

    @Override
    public Unit<? extends Quantity> parseSingleUnit(CharSequence csq, ParsePosition pos) throws ParseException {
	return UnitFormat.getInstance().parseSingleUnit(csq, pos);
    }

    @Override
    public void label(Unit<?> unit, String label) {
	UnitFormat.getInstance().label(unit, label);
    }

    @Override
    public void alias(Unit<?> unit, String alias) {
	UnitFormat.getInstance().alias(unit, alias);
    }

    @Override
    public boolean isValidIdentifier(String name) {
	return UnitFormat.getInstance().isValidIdentifier(name);
    }
}
