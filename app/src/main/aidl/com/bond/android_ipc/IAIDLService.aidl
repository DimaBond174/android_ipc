// IAIDLService.aidl
package com.bond.android_ipc;

// Declare any non-default types here with import statements

interface IAIDLService {

    void onStartCASE_Big_Byte_Array(String  test_file,
      boolean  check_by_recipient) ;

    void onStartCASE_Strings(int  test_size);

    //AIDL can't: void receiveCASE_Big_Byte_Array(byte[]  data);
    void receiveCASE_Big_Byte_Array(String  data_encoded_base64);

    void receiveCASE_Strings(String  str);

}
