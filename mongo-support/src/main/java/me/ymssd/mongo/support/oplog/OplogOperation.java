package me.ymssd.mongo.support.oplog;

/**
 * Mongo oplog operation
 */
public enum OplogOperation {
    NoOp("n"), Insert("i"), Update("u"), Delete("d"), Unknown("!");

    private final char code;

    OplogOperation(String code) {
        this.code = code.charAt(0);
    }

    public char getCode() {
        return code;
    }

    public static OplogOperation find(String code) {
        if (code == null || code.length() == 0) {
            return OplogOperation.Unknown;
        }
        for (OplogOperation value : OplogOperation.values()) {
            if (value.getCode() == code.charAt(0)) {
                return value;
            }
        }
        return OplogOperation.Unknown;
    }
}
