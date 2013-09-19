/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.util.function;

import com.google.common.base.Predicate;


/**
 * simple double specific function with primitive and wrapper handling
 *
 * @author Samuel Gratzl
 *
 */
public class DoublePredicates {
	public static final IDoublePredicate isNaN = new CompareDoublePredicate(6, 0);

	public static IDoublePredicate lt(double v) {
		return new CompareDoublePredicate(0, v);
	}

	public static IDoublePredicate le(double v) {
		return new CompareDoublePredicate(1, v);
	}

	public static IDoublePredicate gt(double v) {
		return new CompareDoublePredicate(2, v);
	}
	public static IDoublePredicate ge(double v) {
		return new CompareDoublePredicate(3, v);
	}

	public static IDoublePredicate eq(double v) {
		return new CompareDoublePredicate(4, v);
	}

	public static IDoublePredicate ne(double v) {
		return new CompareDoublePredicate(5, v);
	}

	public static IDoublePredicate not(final IDoublePredicate o) {
		return new IDoublePredicate() {
			@Override
			public boolean apply(Double arg0) {
				return !o.apply(arg0);
			}

			@Override
			public boolean apply(double in) {
				return !o.apply(in);
			}
		};
	}

	public static IDoublePredicate wrap(final Predicate<Double> p) {
		return new IDoublePredicate() {
			@Override
			public boolean apply(Double arg0) {
				return p.apply(arg0);
			}

			@Override
			public boolean apply(double in) {
				return apply(Double.valueOf(in));
			}
		};
	}

	private static class CompareDoublePredicate implements IDoublePredicate {
		private final int mode;
		private final double v;

		public CompareDoublePredicate(int mode, double v) {
			this.mode = mode;
			this.v = v;
		}

		@Override
		public boolean apply(Double arg0) {
			return apply(arg0.doubleValue());
		}

		@Override
		public boolean apply(double in) {
			switch (mode) {
			case 0:
				return in < v;
			case 1:
				return in <= v;
			case 2:
				return in > v;
			case 3:
				return in >= v;
			case 4:
				return in != v;
			case 5:
				return in == v;
			case 6:
				return Double.isNaN(in);
			}
			return false;
		}

	}
}
