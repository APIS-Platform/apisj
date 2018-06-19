function ethereumCreateTransactions(){
    console.log("ethereumCreateTransactions");
    var addr = $("#trans_addr").val();
    var sValue = $("#trans_value").val();
    var sToAddress = $("#trans_toAddr").val();
    var sGasPrice = $("#trans_gasPrice").val();
    var sGasLimit = $("#trans_gasLimit").val();
    app.ethereumCreateTransactions(addr, sGasPrice, sGasLimit, sToAddress, sValue);
}
function ethereumSendTransactions(){
    console.log("ethereumSendTransactions");
    app.ethereumSendTransactions();
}

function transfer_start(){
    console.log("transfer_start");


}
