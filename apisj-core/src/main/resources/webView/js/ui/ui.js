/* ==================================================
 * header / footer - init ui control
 * ================================================== */
function uiInitStatus(){
    // Status mouseover & mouseleave
    $('.minimumBlack').mouseover(function() {
        $('.minimumBlack').css("background","#991D2B");
        $('.statusMinimumBlack').attr("src","img/new/btn_status_minimum_white.png");
    });
    $('.minimumBlack').mouseleave(function() {
        $('.minimumBlack').css("background","transparent");
        $('.statusMinimumBlack').attr("src","img/new/btn_status_minimum_black.png");
    });
    $('.closeBlack').mouseover(function() {
        $('.closeBlack').css("background","#991D2B");
        $('.statusCloseBlack').attr("src","img/new/btn_status_close_white.png");
    });
    $('.closeBlack').mouseleave(function() {
        $('.closeBlack').css("background","transparent");
        $('.statusCloseBlack').attr("src","img/new/btn_status_close_black.png");
    });

    $('.status-minimum').click(function() {
        app.windowMinimize();
    });
    $('.status-close').click(function() {
        app.windowClose();
    });
}

function initHeaderSpan(){
    const headerSpan = document.querySelectorAll('.header-span');
        for(let i=0; i<headerSpan.length; i++) {
            headerSpan[i].style.borderColor = "transparent";
            headerSpan[i].style.color = "#707070";
            headerSpan[i].style.fontWeight = "400";
        }
}
function selectHeaderSpan(index){
    initHeaderSpan();

    const headerSpan = document.querySelectorAll('.header-span');
    headerSpan[index].style.borderColor = "#991D2B";
    headerSpan[index].style.color = "#991D2B";
    headerSpan[index].style.fontWeight = "600";
}
function uiInitHeaderSpan(){
    // HEADER SPAN
    const headerSpan = document.querySelectorAll('.header-span');

    // Wallet onclick
    headerSpan[0].onclick = function() {
        selectHeaderSpan(0);
        locationHref("main", main_start);
    }

    // Transfer onclick
    headerSpan[1].onclick = function() {
        selectHeaderSpan(1);
        locationHref("transfer", transfer_start);
    }

    // Smart Contract onclick
    headerSpan[2].onclick = function() {
        selectHeaderSpan(2);
        locationHref("smartcontract", smartcontract_start);
    }

    // Transaction onclick
    headerSpan[3].onclick = function() {
        selectHeaderSpan(3);
        locationHref("transaction", transaction_start);
    }

    // Address Masking
    headerSpan[4].onclick = function() {
        selectHeaderSpan(4);
        locationHref("addressmasking", addressmasking_start);
    }
}


/* ==================================================
 * header / footer - method ui control
 * ================================================== */
// hide header and footer (defualt : false)
function setHiddenHeaderAndFooter(bool){
    if(bool){//hide
        $("header").hide();
        $("footer").hide();
    }else{//show
        $("header").show();
        $("footer").show();
    }
}

// setting footer total balance
function setFooterTotalBalance(balance){
    balance = addDotWidthIndex(balance);
    $("footer font").eq(0).text(balance.split(".")[0]+".");
    $("footer font").eq(1).text(balance.split(".")[1]);
}

// setting footer peer number
function setPeerNumber(peerNum){
    var innerHTML = peerNum + " peers";
    $("#footer-block-peers").text(innerHTML);
}

// setting footer block number
function setFooterBlockNumber(lastBlock, bestBlock){
    var diff = bestBlock - lastBlock;
    var lastBlock = comma(lastBlock);
    var innerHTML = lastBlock;
    if(diff != 0){
        innerHTML = innerHTML + "(+"+diff+")";
    }
    $("#footer-block-number").text(innerHTML);
}

// setting footer block timestamp
function setFooterBlockTimestamp(lastBlockTimestamp, nowTimestamp){
    var diffTimestamp = nowTimestamp - lastBlockTimestamp;
    var diffTime = parseInt(diffTimestamp/1000) - 10; // -10 is
    diffTime = Math.max(diffTime, 0);
    var text = "";

    if( diffTime >= 86400){
        //day
        text = text + parseInt(diffTime / 86400) + "day";
    }else {
        // h m s
        var h, m, s;
        var temp = diffTime;
        var print_type = "s";

        // h
        temp = parseInt(diffTime / 3600);
        diffTime = diffTime - temp * 3600;
        h = temp + "h ";
        if(temp > 0){
            print_type = "h";
        }

        // m
        temp = parseInt(diffTime / 60);
        diffTime = diffTime - temp * 60;
        m = temp + "m ";
        if(print_type != "h" && temp > 0){
            print_type = "m";
        }

        // s
        temp = diffTime;
        s = temp + "s ";

        // print
        if(print_type == "h"){
            text = h + m + s;
        }else if(print_type == "m"){
            text = m + s;
        }else {
            text = s;
        }
    }
    text = text + "since last block";
    $("#footer-block-timestamp").text(text);
}





/* ==================================================
 * main - init ui control
 * ================================================== */
function uiInitMainTopSpan(){
    if(pageNames[pageNames.length-1] != "main"){
        return;
    }

    // TOP SPAN
    const topSpan = document.querySelectorAll('.topSpan');

    // APIS
    topSpan[0].onclick = function() {
        for(let i=0; i<topSpan.length; i++) {
            topSpan[i].style.borderColor = "transparent";
            topSpan[i].style.fontWeight = "400";
        }
        topSpan[0].style.borderColor = "#991D2B";
        topSpan[0].style.fontWeight = "600";
    }

    // Mineral
    topSpan[1].onclick = function() {
        for(let i=0; i<topSpan.length; i++) {
            topSpan[i].style.borderColor = "transparent";
            topSpan[i].style.fontWeight = "400";
        }
        topSpan[1].style.borderColor = "#991D2B";
        topSpan[1].style.fontWeight = "600";
    }

    // Tokens
    topSpan[2].onclick = function() {
        for(let i=0; i<topSpan.length; i++) {
            topSpan[i].style.borderColor = "transparent";
            topSpan[i].style.fontWeight = "400";
        }
        topSpan[2].style.borderColor = "#991D2B";
        topSpan[2].style.fontWeight = "600";
    }
}

function uiInitMainBottomSpan(){
    if(pageNames[pageNames.length-1] != "main"){
        return;
    }

    // BOTTOM SPAN
    const bottomSpan = document.querySelectorAll('.bottomSpan');

    // Bottom Wallet
    bottomSpan[0].onclick = function() {
        for(let i=0; i<bottomSpan.length; i++) {
            bottomSpan[i].style.borderColor = "transparent";
            bottomSpan[i].style.color = "#707070";
            bottomSpan[i].style.fontWeight = "400";
        }
        bottomSpan[0].style.borderColor = "#991D2B";
        bottomSpan[0].style.color = "#991D2B";
        bottomSpan[0].style.fontWeight = "600";
    }

    // Bottom APIS & Tokens
    bottomSpan[1].onclick = function() {
        for(let i=0; i<bottomSpan.length; i++) {
            bottomSpan[i].style.borderColor = "transparent";
            bottomSpan[i].style.color = "#707070";
            bottomSpan[i].style.fontWeight = "400";
        }
        bottomSpan[1].style.borderColor = "#991D2B";
        bottomSpan[1].style.color = "#991D2B";
        bottomSpan[1].style.fontWeight = "600";
    }
}

function uiInitWalletList(){

    if(pageNames[pageNames.length-1] != "main"){
        return;
    }

    // Wallet List
    const walletList = document.querySelectorAll('.walletList');
    const walletDetailsAll = document.querySelectorAll('.walletDetailsAll');
    const checkedImg = document.querySelectorAll('#checkedImg');
    const uncheckedImg = document.querySelectorAll('#uncheckedImg');
    const selectedBorder = document.querySelectorAll('.walletList td:first-child');


    // Modal
    const copyAddrModal = document.querySelectorAll('#copyAddrModal');
    const copyAddrContent = document.querySelectorAll('.copyAddrContent');
    const walletAddress = document.querySelectorAll(".walletAddress");
    const copyConfirm = document.getElementById('confirmSpan');
    const copyWalletAddress = document.getElementById('copyWalletAddress');

    // add row checkbox
    if(checkedImg.length > 0){
        for(let i=0; i<checkedImg.length; i++) {
            checkedImg[i].style.display = "none";
            uncheckedImg[i].style.display = "block";
            uncheckedImg[i].style.left = "20px";
            selectedBorder[i].style.borderColor = "transparent";
        }

        checkedImg[0].style.display = "block";
        uncheckedImg[0].style.display = "none";
        checkedImg[0].style.left = "20px";
        selectedBorder[0].style.borderColor = "#97222F";
        walletDetailsAll[0].style.display = "table-row-group";

        // For Box Shadowing
        $(".walletDetailsAll tr:first-child td").each(function(){
            $(this).css("boxShadow", "inset 0px 11px 8px -10px rgba(0, 0, 0, 0.2)");
        });

        walletList[walletList.length-1].style.borderBottom = "none";
        if(0 == walletList.length-1) {
            walletList[walletList.length-1].style.borderBottom = "1px solid #D7DAE2";
        }
    }

    // Init Setting
    for(let k=0; k<walletList.length; k++) {
        uncheckedImg[k].onclick = function() {

            // unchecked all
            walletUnCheckAll();

            // checked target wallet
            walletCheck(k);
        }
    }

    for(let k=0; k<walletList.length; k++) {
        checkedImg[k].onclick = function() {
            walletUnCheck(k);
        }
    }

    // Copy Modal
    for(let i=0; i<walletList.length; i++) {
        walletList[i].childNodes[5].onclick = function() {
            copyWalletAddress.innerHTML = walletAddress[i].innerHTML.trim();
            copyAddrModal[0].style.display = "block";
            copyAddrContent[0].style.display = "block";

            let walletAddrClipboard = document.createElement("INPUT");
            walletAddrClipboard.setAttribute("type", "text");
            walletAddrClipboard.setAttribute("value", copyWalletAddress.innerHTML);
            document.body.appendChild(walletAddrClipboard);
            walletAddrClipboard.select();
            document.execCommand('copy');
            document.body.removeChild(walletAddrClipboard);
        }
    }

    copyConfirm.onclick = function() {
        copyAddrContent[0].style.display = "none";
        copyAddrModal[0].style.display = "none";
    }

    for(var i=0; i<checkedImg.length; i++){
        checkedImg[i].style.display = "none";
        uncheckedImg[i].style.display = "block";
        uncheckedImg[i].style.left = "20px";
        selectedBorder[i].style.borderColor = "transparent";
        walletDetailsAll[i].style.display = "none";
    }

    walletList[walletList.length-1].style.borderBottom = "none";

}


/* ==================================================
 * main - method ui control
 * ================================================== */
function walletCheck(index){
    mainCheckListIndex = index;

    // Wallet List
    const walletList = document.querySelectorAll('.walletList');
    const walletDetailsAll = document.querySelectorAll('.walletDetailsAll');
    const checkedImg = document.querySelectorAll('#checkedImg');
    const uncheckedImg = document.querySelectorAll('#uncheckedImg');
    const selectedBorder = document.querySelectorAll('.walletList td:first-child');

    if(index >= checkedImg.length || index < 0){
        return;
    }

    checkedImg[index].style.display = "block";
    uncheckedImg[index].style.display = "none";
    checkedImg[index].style.left = "20px";
    selectedBorder[index].style.borderColor = "#97222F";
    walletDetailsAll[index].style.display = "table-row-group";

    walletList[walletList.length-1].style.borderBottom = "none";
    if(index == walletList.length-1) {
        walletList[walletList.length-1].style.borderBottom = "1px solid #D7DAE2";
    }
}
function walletUnCheck(index){
    mainCheckListIndex = index;

    // Wallet List
    const walletList = document.querySelectorAll('.walletList');
    const walletDetailsAll = document.querySelectorAll('.walletDetailsAll');
    const checkedImg = document.querySelectorAll('#checkedImg');
    const uncheckedImg = document.querySelectorAll('#uncheckedImg');
    const selectedBorder = document.querySelectorAll('.walletList td:first-child');

    checkedImg[index].style.display = "none";
    uncheckedImg[index].style.display = "block";
    uncheckedImg[index].style.left = "20px";
    selectedBorder[index].style.borderColor = "transparent";
    walletDetailsAll[index].style.display = "none";
    walletList[walletList.length-1].style.borderBottom = "none";

}
function walletUnCheckAll(){
    mainCheckListIndex = -1;

    // Wallet List
    const walletList = document.querySelectorAll('.walletList');
    const walletDetailsAll = document.querySelectorAll('.walletDetailsAll');
    const checkedImg = document.querySelectorAll('#checkedImg');
    const uncheckedImg = document.querySelectorAll('#uncheckedImg');
    const selectedBorder = document.querySelectorAll('.walletList td:first-child');

    for(let i=0; i<checkedImg.length; i++) {
        checkedImg[i].style.display = "none";
        uncheckedImg[i].style.display = "block";
        uncheckedImg[i].style.left = "20px";
        selectedBorder[i].style.borderColor = "transparent";
        walletDetailsAll[i].style.display = "none";
    }

}
function setTotalBalance(balance){
    balance = addDotWidthIndex(balance);
    $("#APISNum font").eq(0).text(balance.split(".")[0]+".");
    $("#APISNum font").eq(1).text(balance.split(".")[1]);
}

function setTotalMineral(mineral){

    mineral = addDotWidthIndex(mineral);
    $("#amountMnr font").eq(0).text(mineral.split(".")[0]+".");
    $("#amountMnr font").eq(1).text(mineral.split(".")[1]);
}




/* ==================================================
 * start javascript
 * ================================================== */
function ui_start(){
    uiInitStatus();
    uiInitHeaderSpan();
}