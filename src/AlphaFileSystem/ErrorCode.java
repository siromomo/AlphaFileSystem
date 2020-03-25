package AlphaFileSystem;

import java.util.*;

public class ErrorCode extends RuntimeException {
    public static final int IO_EXCEPTION = 1;
    public static final int CHECKSUM_CHECK_FAILED = 2;
    public static final int GET_FILE_DATA_FAILED = 3;
    public static final int GET_META_DATA_FAILED = 4;
    public static final int GET_BLOCK_META_FAILED = 5;
    public static final int READ_OUT_BOUND = 6;
    public static final int GET_BLOCK_COUNT_FAILED = 7;
    public static final int GET_REMOTE_MANAGER_FAILED = 8;
    public static final int GET_DATA_FROM_REMOTE_FAILED = 9;
    public static final int TRANSPORT_DATA_TO_REMOTE_FAILED = 10;
    public static final int REQUEST_A_BLOCK_NOT_EXIST = 11;
    public static final int CURR_LOGIC_BLOCK_NOT_VALID = 12;

    public static final int UNKNOWN = 100;

    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();
    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO exception");
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block checksum check failed: there is a block broken");
        ErrorCodeMap.put(GET_FILE_DATA_FAILED, "get origin data from file failed because all the block are not valid");
        ErrorCodeMap.put(GET_META_DATA_FAILED, "get file meta data failed");
        ErrorCodeMap.put(GET_BLOCK_META_FAILED, "get block meta data failed");
        ErrorCodeMap.put(READ_OUT_BOUND, "cursor plus read's length is out of bound");
        ErrorCodeMap.put(GET_BLOCK_COUNT_FAILED, "get block count id failed");
        ErrorCodeMap.put(GET_REMOTE_MANAGER_FAILED, "get remote block/file manager failed");
        ErrorCodeMap.put(GET_DATA_FROM_REMOTE_FAILED, "get data from remote failed");
        ErrorCodeMap.put(TRANSPORT_DATA_TO_REMOTE_FAILED, "transport data to remote failed");
        ErrorCodeMap.put(REQUEST_A_BLOCK_NOT_EXIST, "request a block that not exist");
        ErrorCodeMap.put(CURR_LOGIC_BLOCK_NOT_VALID, "the block list in the logic block are all invalid");

        ErrorCodeMap.put(UNKNOWN, "unknown");
    }

    public static String getErrorText(int errorCode){
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }

    private int errorCode;

    public ErrorCode(int errorCode){
        super(String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode)));
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
