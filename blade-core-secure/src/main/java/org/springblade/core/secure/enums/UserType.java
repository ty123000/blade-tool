package org.springblade.core.secure.enums;

public enum UserType {
	/**
	 * 管理员
	 */
	ADMIN((byte) 0),
	/**
	 * 用户
	 */
	USER((byte) 1),
	/**
	 * 代理商
	 */
	AGENT((byte) 2),
	;

	private final byte value;

	UserType(byte value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static UserType of(final byte value) {
		for (UserType userType : values()) {
			if (userType.value == value) {
				return userType;
			}
		}
		return null;
	}

	public static UserType of(final int value) {
		return of((byte) value);
	}

}
