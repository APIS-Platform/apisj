/* ==================================================
 * method
 * ================================================== */




/* ==================================================
 * 기능 테스트 용 메소드
 * ================================================== */
function ethereumCreateTransactions(){
    console.log("ethereumCreateTransactions");
    var addr = $("#trans_addr").val();
    var sValue = $("#trans_value").val();
    var sValueUnit = $("#trans_valueUnit").val();
    var sToAddress = $("#trans_toAddr").val();
    var sGasPrice = $("#trans_gasPrice").val();
    var sGasUnit = $("#trans_gasUnit").val();
    var sPasswd = $("#trans_passwd").val();
    app.ethereumCreateTransactions(addr, sGasPrice, sGasUnit, sToAddress, sValue, sValueUnit, sPasswd);
}
function ethereumSendTransactions(){
    app.ethereumSendTransactions();
}


function initSelectWalletList(){
    // load wallet list
    getKeyStoreDataListAllWithJson("asc");

    var html = "";
        html = html + "<option value=''>(지갑선택)</option>";
    for(var i=0; i<keystoreList.length; i++){
        html = html + "<option value='"+keystoreList[i].address+"'>";
        html = html + keystoreList[i].alias;
        html = html + "</option>";
    }
    $("#trans_addr_select").html(html);
}

function onChangeSelectWallet(){
    $("#trans_addr").val($("#trans_addr_select").val());
}


function transfer_start(){
    // header top select (1 is transfer)
    selectHeaderSpan(1);

    console.log("transfer_start");
}
