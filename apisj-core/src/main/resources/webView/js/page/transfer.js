/* ==================================================
 * method
 * ================================================== */
function createTransferSelectWallet(selector){
    var x, i, j, selElmnt, a, b, c, container, img, alias, address, mask, arrow, balance, mineral;
    x = document.getElementsByClassName(selector);

    for (i = 0; i < x.length; i++) {
        selElmnt = x[i].getElementsByTagName("select")[0];
        a = document.createElement("DIV");
        a.setAttribute("class", "select-selected");

        container = document.createElement("DIV");
        container.setAttribute("class", "select-container");
        a.appendChild(container);

        img = document.createElement("IMG");
        img.setAttribute("class", "img");
        container.appendChild(img);

        alias = document.createElement("FONT");
        alias.setAttribute("class", "alias");
        alias.innerHTML = selElmnt.options[selElmnt.selectedIndex].getAttribute("alias");
        container.appendChild(alias);

        address = document.createElement("FONT");
        address.setAttribute("class", "address");
        address.innerHTML = selElmnt.options[selElmnt.selectedIndex].getAttribute("address");
        container.appendChild(address);

        mask = document.createElement("FONT");
        mask.setAttribute("class", "mask");
        mask.innerHTML = selElmnt.options[selElmnt.selectedIndex].getAttribute("mask");
        container.appendChild(mask);

        arrow = document.createElement("IMG");
        arrow.setAttribute("class", "arrow");
        arrow.setAttribute("src", "img/new/btn_dropdowngrey.png");
        arrow.innerHTML = selElmnt.options[selElmnt.selectedIndex].getAttribute("arrow");
        container.appendChild(arrow);

        balance = document.createElement("input");
        balance.setAttribute("class", "balance");
        balance.setAttribute("type", "hidden");
        balance.setAttribute("value", selElmnt.options[selElmnt.selectedIndex].getAttribute("balance"));
        container.appendChild(balance);

        mineral = document.createElement("input");
        mineral.setAttribute("class", "mineral");
        mineral.setAttribute("type", "hidden");
        mineral.setAttribute("value", selElmnt.options[selElmnt.selectedIndex].getAttribute("mineral"));
        container.appendChild(mineral);

        x[i].appendChild(a);
        b = document.createElement("DIV");
        b.setAttribute("class", "select-items select-hide");

        for (j = 0; j < selElmnt.length; j++) {
            c = document.createElement("DIV");
            c.setAttribute("value", selElmnt.options[j].value);
            if(j == selElmnt.selectedIndex){
                c.setAttribute("class", "same-as-selected");
            }

            container = document.createElement("DIV");
            container.setAttribute("class", "select-container");
            c.appendChild(container);

            img = document.createElement("IMG");
            img.setAttribute("class", "img");
            container.appendChild(img);

            alias = document.createElement("FONT");
            alias.setAttribute("class", "alias");
            alias.innerHTML = selElmnt.options[j].getAttribute("alias");
            container.appendChild(alias);

            address = document.createElement("FONT");
            address.setAttribute("class", "address");
            address.innerHTML = selElmnt.options[j].getAttribute("address");
            container.appendChild(address);

            mask = document.createElement("FONT");
            mask.setAttribute("class", "mask");
            mask.innerHTML = selElmnt.options[j].getAttribute("mask");
            container.appendChild(mask);

            arrow = document.createElement("IMG");
            arrow.setAttribute("class", "arrow");
            arrow.setAttribute("src", "img/new/btn_dropdowngrey.png");
            arrow.innerHTML = selElmnt.options[selElmnt.selectedIndex].getAttribute("arrow");
            container.appendChild(arrow);

            balance = document.createElement("input");
            balance.setAttribute("class", "balance");
            balance.setAttribute("type", "hidden");
            balance.setAttribute("value", selElmnt.options[selElmnt.selectedIndex].getAttribute("balance"));
            container.appendChild(balance);

            mineral = document.createElement("input");
            mineral.setAttribute("class", "mineral");
            mineral.setAttribute("type", "hidden");
            mineral.setAttribute("value", selElmnt.options[selElmnt.selectedIndex].getAttribute("mineral"));
            container.appendChild(mineral);

            c.addEventListener("click", function(e) {
                var y, i, k, s, h;
                s = this.parentNode.parentNode.getElementsByTagName("select")[0];
                h = this.parentNode.previousSibling;
                for (i = 0; i < s.length; i++) {
                    if (s.options[i].value == this.getAttribute("value")) {
                        s.selectedIndex = i;
                        h.innerHTML = this.innerHTML;
                        y = this.parentNode.getElementsByClassName("same-as-selected");
                        for (k = 0; k < y.length; k++) {
                            y[k].removeAttribute("class");
                        }
                        this.setAttribute("class", "same-as-selected");

                        // change transfer amount title
                        setTransferAmountWalletAddress($(".transfer-wallet-select select option:selected").attr("address"));
                        setTransferAmountBalance($(".transfer-wallet-select select option:selected").attr("balance"));
                        setTransferAmountTotalMineral($(".transfer-wallet-select select option:selected").attr("mineral"));
                        break;
                    }
                }
                h.click();
            });
            b.appendChild(c);
        }
        x[i].appendChild(b);
        a.addEventListener("click", function(e) {
            e.stopPropagation();
            closeAllSelect(this);
            this.nextSibling.classList.toggle("select-hide");
            this.classList.toggle("select-arrow-active");
        });
    }

    function closeAllSelect(elmnt) {
        var x, y, i, arrNo = [];
        x = document.getElementsByClassName("select-items");
        y = document.getElementsByClassName("select-selected");
        for (i = 0; i < y.length; i++) {
            if (elmnt == y[i]) {
                arrNo.push(i)
            } else {
                y[i].classList.remove("select-arrow-active");
            }
        }
        for (i = 0; i < x.length; i++) {
            if (arrNo.indexOf(i)) {
                x[i].classList.add("select-hide");
            }
        }
    }
    document.addEventListener("click", closeAllSelect);

    function initStyleSelect(){
        var html = '';
        html = html + '<style>';
        html = html + '    /* scrollbar css */';
        html = html + '    ::-webkit-scrollbar { width: 4px; }';
        html = html + '    ::-webkit-scrollbar-track { border-radius: 4px; }';
        html = html + '    ::-webkit-scrollbar-thumb { background: #999999; border-radius: 4px; }';
        html = html + '    ::-webkit-scrollbar-thumb:hover { background: #555; }';

        html = html + '    /* custom select */';
        html = html + '    .' + selector + ' { position: relative; font-family: Arial; }';
        html = html + '    .' + selector + ' select { display: none; }';
        html = html + '    .' + selector + ' .select-selected { color: #000000; padding: 0; cursor: pointer; text-align:center; background-color: transparent; }';
        html = html + '    .' + selector + ' .select-selected:after { position: absolute; content: ""; top: 14px; right: 10px; width: 0; height: 0; border: 6px solid transparent; border-color: #fff transparent transparent transparent; }';
        html = html + '    .' + selector + ' .select-selected.select-arrow-active:after { border-color: transparent transparent #fff transparent; top: 7px; }';

        html = html + '    .' + selector + ' .select-items div { color: #000000; padding: 0; cursor: pointer; text-align:center;  }';
        html = html + '    .' + selector + ' .select-items { position: absolute; max-height: 143px; background-color: #ffffff; top: 110%; left: 0; right: 0; z-index: 99; box-shadow: 0px 4px 6px 3px rgba(0,0,0,0.2); overflow-y: auto; overflow-x: hidden; }';
        html = html + '    .' + selector + ' .select-hide { display: none; }';
        html = html + '    .' + selector + ' .select-items div:hover, .same-as-selected { background-color: #f2f2f2; }';

        html = html + '    .' + selector + ' .select-container{ position: relative; display: inline-block; margin-top:12px; text-align:left; }';
        html = html + '    .' + selector + ' .select-container .img{ position: absolute; top:0px; left:0px; width:24px; height:24px; border-radius: 12px; background: #910000; }';
        html = html + '    .' + selector + ' .select-container .alias{ position: absolute; top:0px; left:32px; font-weight: 400; color:#353535; }';
        html = html + '    .' + selector + ' .select-container .address{ position: absolute; bottom:0px; left:32px; font-weight: 300; color:#999999; }';
        html = html + '    .' + selector + ' .select-container .arrow{ position: absolute; top:9px; right:0px; }';
        html = html + '    .' + selector + ' .select-selected .select-container .mask{ position: absolute; top:6px; right:18px; font-weight: 400; color:#2b2b2b}';
        html = html + '    .' + selector + ' .select-items .select-container .mask{ position: absolute; top:6px; right:17px; font-weight: 400; color:#2b2b2b}';
        html = html + '    .' + selector + ' .select-items .select-container .arrow{ display:none;}';

        html = html + '</style>';
        $(x).before(html);
    }

    initStyleSelect();
    $("."+selector+" .select-selected").css("height","100%").css("text-align","center");
    $("."+selector+" .select-selected div").css("width", ($(".select-selected").width()-32)+"px");
    $("."+selector+" .select-selected div").css("height", ($(".select-selected").height()-24)+"px");
    $("."+selector+" .select-items div").css("height", $(".select-selected").css("height"));
    $("."+selector+" .select-items div div").css("width", ($(".select-selected").width()-32)+"px");
    $("."+selector+" .select-items div div").css("height", ($(".select-selected").height()-24)+"px");
}


function createTransferSelectPer(selector){

    var x, i, j, selElmnt, a, b, c, arrow;
    x = document.getElementsByClassName(selector);
    for (i = 0; i < x.length; i++) {
        selElmnt = x[i].getElementsByTagName("select")[0];
        a = document.createElement("DIV");
        a.setAttribute("class", "select-selected");
        a.innerHTML = selElmnt.options[selElmnt.selectedIndex].innerHTML;

        arrow = document.createElement("IMG");
        arrow.setAttribute("class", "arrow");
        arrow.setAttribute("src", "img/new/btn_dropdownred.png");
        a.appendChild(arrow);

        x[i].appendChild(a);
        b = document.createElement("DIV");
        b.setAttribute("class", "select-items select-hide");

        for (j = 0; j < selElmnt.length; j++) {
            c = document.createElement("DIV");
            c.setAttribute("value", selElmnt.options[j].value);
            c.innerHTML = selElmnt.options[j].innerHTML;

            arrow = document.createElement("IMG");
            arrow.setAttribute("class", "arrow");
            arrow.setAttribute("src", "img/new/btn_dropdownred.png");
            c.appendChild(arrow);

            if(j == selElmnt.selectedIndex){
                c.setAttribute("class", "same-as-selected");
            }

            c.addEventListener("click", function(e) {
                var y, i, k, s, h;
                s = this.parentNode.parentNode.getElementsByTagName("select")[0];
                h = this.parentNode.previousSibling;
                for (i = 0; i < s.length; i++) {
                    if (s.options[i].value == this.getAttribute("value")) {
                        s.selectedIndex = i;
                        h.innerHTML = this.innerHTML;
                        y = this.parentNode.getElementsByClassName("same-as-selected");
                        for (k = 0; k < y.length; k++) {
                            y[k].removeAttribute("class");
                        }
                        this.setAttribute("class", "same-as-selected");

                        setTransferAmount();
                        settingTransferData();
                        break;
                    }
                }
                h.click();
            });
            b.appendChild(c);
        }
        x[i].appendChild(b);
        a.addEventListener("click", function(e) {
            e.stopPropagation();
            closeAllSelect(this);
            this.nextSibling.classList.toggle("select-hide");
            this.classList.toggle("select-arrow-active");
        });
    }

    function closeAllSelect(elmnt) {
        var x, y, i, arrNo = [];
        x = document.getElementsByClassName("select-items");
        y = document.getElementsByClassName("select-selected");
        for (i = 0; i < y.length; i++) {
            if (elmnt == y[i]) {
                arrNo.push(i)
            } else {
                y[i].classList.remove("select-arrow-active");
            }
        }
        for (i = 0; i < x.length; i++) {
            if (arrNo.indexOf(i)) {
                x[i].classList.add("select-hide");
            }
        }
    }
    document.addEventListener("click", closeAllSelect);

    function initStyleSelect(){
        var html = '';
        html = html + '<style>';

        html = html + '    /* custom select */';
        html = html + '    .' + selector + ' { position: relative; font-family: Arial; }';
        html = html + '    .' + selector + ' select { display: none; }';
        html = html + '    .' + selector + ' .select-selected { padding: 9px 6px; padding-right:0px; border: 1px solid #910000; border-radius: 4px; background-color: #fafafa; text-align:right; font-size: 12px; color: #910000; cursor: pointer; }';
        html = html + '    .' + selector + ' .select-selected.select-arrow-active:after { border-color: transparent transparent #fff transparent; top: 7px; }';

        html = html + '    .' + selector + ' .select-items div { color: #000000; padding: 0; cursor: pointer; text-align:center; font-size: 12px; padding: 10px; }';
        html = html + '    .' + selector + ' .select-items { position: absolute; background-color: #ffffff; top: 110%; left: 0; right: 0; z-index: 99; box-shadow: 0px 4px 6px 3px rgba(0,0,0,0.2); overflow-y: auto; overflow-x: hidden; }';
        html = html + '    .' + selector + ' .select-hide { display: none; }';
        html = html + '    .' + selector + ' .select-items div:hover, .same-as-selected { background-color: #f2f2f2; }';

        html = html + '    .' + selector + ' .arrow{ margin-left:6px; margin-right:8px; }';
        html = html + '    .' + selector + ' .select-items .arrow{ display:none;}';

        html = html + '</style>';
        $(x).before(html);
    }

    initStyleSelect();
}

function onClickTransfer(){
    showTransferModal();
}



/* ==================================================
 * 기능 테스트 용 메소드
 * ================================================== */
function ethereumCreateTransactions(){
    console.log("ethereumCreateTransactions");
    var addr = $("#transfer_wallet_address").val();
    var sValue = $("#transfer_transfer_amount").val();
    var sToAddress = $("#trans_toAddr").val();
    var sGasPrice = $("#trans_gasPrice").val();
    var sPasswd = $("#trans_passwd").val();
    app.ethereumCreateTransactions(addr, sGasPrice, sToAddress, sValue, sPasswd);
}

function ethereumCreateTransactionsWithMask(){
    console.log("ethereumCreateTransactions");
    var addr = $("#trans_addr").val();
    var sValue = $("#trans_value").val();
    var sMask = $("#trans_toAddr_mask").val();
    var sGasPrice = $("#trans_gasPrice").val();
    var sPasswd = $("#trans_passwd").val();
    app.ethereumCreateTransactionsWithMask(addr, sGasPrice, sMask, sValue, sPasswd);
}

function ethereumSendTransactions(){
    app.ethereumSendTransactions();
}

function setTransferAmountWalletAddress(address){
    if (typeof(address) != "undefined" && $(address) != null){
        $("#transfer_wallet_address").val(address);
    }
}

function setTransferAmountToAddress(address){
    if (typeof(address) != "undefined" && $(address) != null){
        $("#transfer_to_address").val(address);
    }
}

function setTransferAmountBalance(balance){
    if (typeof(balance) != "undefined" && $(balance) != null){
        balance = addDotWidthIndex(balance);
        $(".transfer-amount-title .natural").text(balance.split(".")[0]+".");
        $(".transfer-amount-title .decimal").text(appendTextZero(balance.split(".")[1]));

        $("#transfer_transfer_amount").val(balance.replace(".",""));
        setTransferAmount();
        settingTransferData();
    }
}

function setTransferAmount(amount){
    var balance = $(".transfer-wallet-select select option:selected").attr("balance");
    var per = $(".transfer-per-select select option:selected").val();
    var transferAmount = BigInteger("0");
    if(amount && amount.length > 0){
        transferAmount = BigInteger(amount);
    }else{
        transferAmount = BigInteger(balance);
        transferAmount = transferAmount.divide("100").multiply(per);
    }
    var amount = addDotWidthIndex(transferAmount.toString());

    var natural = amount.split(".")[0];
    var decimal = amount.split(".")[1];
    $(".transfer-amount .natural").text(natural+".");
    $(".transfer-amount .decimal").text(appendTextZero(decimal));
    $("#transferAmount").val(natural + "." + decimal);
    $("#transfer_transfer_amount").val(natural+decimal);
}

function setTransferAmountTotalMineral(mineral){
    mineral = addDotWidthIndex(mineral);
    var natural = mineral.split(".")[0];
    var decimal = mineral.split(".")[1];
    $(".gas-price-total .natural").text(natural + ".");
    $(".gas-price-total .decimal").text(decimal);
}

function initSelectWalletList(){

    if(pageNames[pageNames.length - 1] != "transfer"){
        return;
    }

    // load wallet list
    getKeyStoreDataListAllWithJson("asc");
    var selectedIndex = $(".transfer-wallet-select select option:selected").index();
    var html = "<select>";
    for(var i=0; i<keystoreList.length; i++){
        html = html + "<option ";
        html = html + "alias='"+keystoreList[i].alias+"' ";
        html = html + "address='"+keystoreList[i].address+"' ";
        html = html + "mask='' ";
        html = html + "value='"+keystoreList[i].address+"' ";
        html = html + "balance='"+keystoreList[i].balance+"' ";
        html = html + "mineral='"+keystoreList[i].mineral+"' ";
        if(i == selectedIndex){
            html = html + "selected='selected' ";
        }
        html = html + "></option>";
    }
    html = html + "</select>";
    $(".transfer-wallet-select").html(html);

    // create custom select
    createTransferSelectWallet("transfer-wallet-select");

    // change transfer amount title
    setTransferAmountWalletAddress($(".transfer-wallet-select select option:selected").attr("address"));
    setTransferAmountBalance($(".transfer-wallet-select select option:selected").attr("balance"));
    setTransferAmountTotalMineral($(".transfer-wallet-select select option:selected").attr("mineral"));
}

function settingTransferData(){
    var totalBalance = $("#transfer_total_balance").val();
    var totalMineral = $("#transfer_total_mineral").val();
    var gasPrice = $("#transfer_gas_price").val();
    if(gasPrice == "" || gasPrice == "0"){
        gasPrice = "50000000000";
        $("#transfer_gas_price").val(gasPrice);
    }
    var transferAmount = $("#transfer_transfer_amount").val();
    var totalFee = BigInteger(gasPrice).multiply("200000").subtract(totalMineral).toString();
    var totalSend = BigInteger(transferAmount).add(totalFee).toString();
    totalSend = (totalSend.indexOf("-") >= 0) ? "0.000000000000000000" : totalSend;

    $("#transfer_total_fee").val(totalFee);
    $("#transfer_total_send").val(totalSend);

    // Left Total Mineral
    var dotTotalMineral = addDotWidthIndex(totalMineral);
    $(".gas-price-total .natural").text( dotTotalMineral.split(".")[0] + "." );
    $(".gas-price-total .decimal").text( appendTextZero(dotTotalMineral.split(".")[1]) );

    // Right Total Send
    var dotTotalSend = addDotWidthIndex(totalSend);
    $(".total_send .natural").text( comma(dotTotalSend.split(".")[0]) );
    $(".total_send .decimal").text( "."+appendTextZero(dotTotalSend.split(".")[1]) );


    $(".gas-price .result .natural1").text( dotTotalSend.split(".")[0] +".");
    $(".gas-price .result .decimal1").text( appendTextZero(dotTotalSend.split(".")[1]) );
}

function onChangeSelectWallet(){
    $("#trans_addr").val($("#trans_addr_select").val());
}


/* ==================================================
 * popup
 * ================================================== */
function uiInitTransferModalContentFooterCycle(){
    var width = $(".transfer_modal_body .content .content-footer").width();
    var html = "";
    html = html + "<ul>";
    for(var i=0; i< width/12 -1 ; i++){
        html = html + "<li></li>";
    }
    html = html + "</ul>";
    $(".transfer_modal_body .content .content-footer").html(html);
}

function initTransferModalData(){
    // transfer_date
    let month = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
    var nowDate = new Date();
    var transfer_date = "";
    transfer_date = transfer_date + month[nowDate.getMonth()] + " " + nowDate.getDate() + ", " + nowDate.getFullYear();
    var hh = (nowDate.getHours().toString().length > 1) ? nowDate.getHours() : "0"+nowDate.getHours();
    var mm = (nowDate.getMinutes().toString().length > 1) ? nowDate.getMinutes() : "0"+nowDate.getMinutes();
    transfer_date = transfer_date + " " + hh + ":" + mm;
    transfer_date = transfer_date + " (UTC +"+nowDate.getTimezoneOffset()/60*(-1)+")";
    $(".transfer_date font").text(transfer_date);

    // Sending Address
    $(".sending_address .right").text(" - ");

    // Receive Address
    $(".receive_address .right").text(" - ");

    // Amount to send
    $(".amount_to_send .right").text("0.000000000000000000 APIS");

    // Total Withdrawal
    $(".total_withdrawal .right").text("0.000000000000000000 APIS");

    // After Balance
    $(".after_balance .right").text("0.000000000000000000 APIS");

    // Confirm Password
    $(".input_pass img.private").attr("src","img/new/icon_private.png");
    $(".input_pass img.private").removeClass("show");
    $(".input_pass input").attr("type", "password");
    $(".input_pass input").val("");
    setTransferModalButtonEnable(false);
}


function showTransferModalRemovePasswordIcon(){
    $(".input_pass img.status").parent().prev().css("margin-right","0px");
    $(".input_pass img.status").parent().show();
}
function hideTransferModalRemovePasswordIcon(){
    $(".input_pass img.status").parent().hide();
    $(".input_pass img.status").parent().prev().css("margin-right","25px");
}

function showTransferModal(){
    console.log("showTransferModal");

    $(".transfer_modal").show();

    // init css
    var width = $(".input_pass").width();
    $(".input_pass input").css("width", (width -55)+"px");

    $(".input_pass .text input").on("keyup", function(){
        if($(this).val().length > 0){
            showTransferModalRemovePasswordIcon();
            setTransferModalButtonEnable(true);
        }else{
            hideTransferModalRemovePasswordIcon();
            setTransferModalButtonEnable(false);
        }
    });
    $( window ).on( 'resize', function( ) {
        var width = $(".input_pass").width();
        $(".input_pass input").css("width", (width -55)+"px");

        uiInitTransferModalContentFooterCycle();
    } );
    uiInitTransferModalContentFooterCycle();
    hideTransferModalRemovePasswordIcon();
    hideTransferModalHintText();
    initTransferModalData();
}
function hideTransferModal(){
    initTransferModalData();
    $(".transfer_modal").hide();
}

function showTransferModalHintText(){
    $(".input_pass .hint").css("opacity", 1);
}
function hideTransferModalHintText(){
    $(".input_pass .hint").css("opacity", 0);
}
function setTransferModalButtonEnable(enable){
    if(enable){
        $(".content-footer a.btn").attr("href","javascript:console.log('실행');");
        $(".content-footer a.btn img").attr("src", "img/new/btn_transferpopup_red.png");;
    }else{
        $(".content-footer a.btn").attr("href","javascript:;");
        $(".content-footer a.btn img").attr("src", "img/new/btn_transferpopup_grey.png");;
    }
}

function onClickTransferModalTogglePrivate(selector){
    if($(".input_pass img.private").hasClass("show")){
        $(".input_pass img.private").attr("src","img/new/icon_private.png");
        $(".input_pass img.private").removeClass("show");

        $(".input_pass input").attr("type", "password");
    }else{
        $(".input_pass img.private").attr("src","img/new/icon_public.png");
        $(".input_pass img.private").addClass("show");

        $(".input_pass input").attr("type", "text");
    }
}

function onClickTransferModalRemovePassword(selector){
    hideTransferModalRemovePasswordIcon();
    setTransferModalButtonEnable(false);
    $(".input_pass input").val("");
}

function onClickTransferModalClose(){
    hideTransferModal();
}



/* ==================================================
 *  start method
 * ================================================== */
function transfer_start(){
    // header top select (1 is transfer)
    selectHeaderDiv(1);

    uiInitTransfer();
    createTransferSelectPer("transfer-per-select");

    // load walletList
    initSelectWalletList();
    settingTransferData();

}