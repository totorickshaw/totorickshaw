package com.digitechsolz.totorickshaw;

public class RestApiUrl {
    String BASE_URL = "https://digitechsolz.com/totorickshaw";


    String LOGIN_URL = BASE_URL + "/login";
    String REGISTRATION_URL = BASE_URL + "/register";
    String COMPLREG_URL = BASE_URL + "/complete-registration";

    String BALANCE_URL = BASE_URL + "/driver/get-wallet-balance";
    String HASH_URL = BASE_URL + "/driver/hash-generator";
    String PLACE_ORDER_URL = BASE_URL + "/driver/wallet-transaction";
}
