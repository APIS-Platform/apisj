let mainCheckListIndex = 0;
let letters = /[a-zA-Z]/g;
let numbers = /[0-9]/g;
let specials = /[^(a-zA-Z0-9)]/g;
let changePwValidate;

/* ==================================================
 * header / footer - init ui control
 * ================================================== */
function uiInitStatus(){
    // Status mouseover & mouseleave
    $('.minimumBlack').mouseover(function() {
        $('.minimumBlack').css("background","#910000");
        $('.statusMinimumBlack').attr("src","img/new/btn_status_minimum_white.png");
    });
    $('.minimumBlack').mouseleave(function() {
        $('.minimumBlack').css("background","transparent");
        $('.statusMinimumBlack').attr("src","img/new/btn_status_minimum_black.png");
    });
    $('.closeBlack').mouseover(function() {
        $('.closeBlack').css("background","#910000");
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

function initHeaderDiv(){
    const headerDiv = document.querySelectorAll('.header-div');
    for(let i=0; i<headerDiv.length; i++) {
        headerDiv[i].style.borderColor = "transparent";
        headerDiv[i].style.color = "#999999";
        headerDiv[i].style.fontWeight = "300";
    }
}
function selectHeaderDiv(index){
    const headerDiv = document.querySelectorAll('.header-div');
    initHeaderDiv();
    headerDiv[index].style.borderColor = "#910000";
    headerDiv[index].style.color = "#910000";
    headerDiv[index].style.fontWeight = "400";
}

function uiInitHeaderDiv(){
    // HEADER SPAN
    const headerDiv = document.querySelectorAll('.header-div');

    // Wallet onclick
    headerDiv[0].onclick = function() {
        selectHeaderDiv(0);
        locationHref("main", main_start);
    }

    // Transfer onclick
    headerDiv[1].onclick = function() {
        selectHeaderDiv(1);
        locationHref("transfer", transfer_start);
    }

    // Smart Contract onclick
    headerDiv[2].onclick = function() {
        selectHeaderDiv(2);
        locationHref("smartcontract", smartcontract_start);
    }

    // Transaction onclick
    headerDiv[3].onclick = function() {
        selectHeaderDiv(3);
        locationHref("transaction", transaction_start);
    }

    // Address Masking
    headerDiv[4].onclick = function() {
        selectHeaderDiv(4);
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
    if (typeof(balance) != "undefined" && $(balance) != null){
        balance = addDotWidthIndex(balance);
        $("footer .natural").text(balance.split(".")[0]+".");
        $("footer .decimal").text(appendTextZero(balance.split(".")[1]));
    }
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
 * intro - init ui control
 * ================================================== */



/* ==================================================
 * intro - method ui control
 * ================================================== */
function uiMethodIntroNavDot(index, max) {
    $("#navSpan span").removeClass("active");
    $("#navSpan span").eq(index).addClass("active");

    if(max == 3) {
        $("#navSpan span").eq(3).addClass("hide");
        $("#navSpan").css("width", "52px");
    }
    if(max == 4) {
        $("#navSpan span").eq(3).removeClass("hide");
        $("#navSpan").css("width", "66px");
    }
}


/* ==================================================
 * main - init ui control
 * ================================================== */
function uiInitMainTopDiv(){
    if(pageNames[pageNames.length-1] != "main"){
        return;
    }

    // TOP DIV
    const topDiv = document.querySelectorAll('.topDiv');

    // APIS
    topDiv[0].onclick = function() {
        for(let i=0; i<topDiv.length; i++) {
            topDiv[i].style.color = "#999999";
            topDiv[i].style.borderColor = "transparent";
            topDiv[i].style.fontWeight = "300";
        }
        topDiv[0].style.color = "#910000";
        topDiv[0].style.borderColor = "#910000";
        topDiv[0].style.fontWeight = "400";
    }

    // Mineral
    topDiv[1].onclick = function() {
        for(let i=0; i<topDiv.length; i++) {
            topDiv[i].style.color = "#999999";
            topDiv[i].style.borderColor = "transparent";
            topDiv[i].style.fontWeight = "300";
        }
        topDiv[1].style.color = "#910000";
        topDiv[1].style.borderColor = "#910000";
        topDiv[1].style.fontWeight = "400";
    }
}

function uiInitMainBottomDiv(){
    if(pageNames[pageNames.length-1] != "main"){
        return;
    }

    // BOTTOM DIV
    const bottomDiv = document.querySelectorAll('.bottomDiv');

    // Bottom Wallet
    bottomDiv[0].onclick = function() {
        for(let i=0; i<bottomDiv.length; i++) {
            bottomDiv[i].style.borderColor = "transparent";
            bottomDiv[i].style.color = "#999999";
            bottomDiv[i].style.fontWeight = "300";
        }
        bottomDiv[0].style.borderColor = "#910000";
        bottomDiv[0].style.color = "#910000";
        bottomDiv[0].style.fontWeight = "400";
    }

    // Bottom APIS & Tokens
    bottomDiv[1].onclick = function() {
        for(let i=0; i<bottomDiv.length; i++) {
            bottomDiv[i].style.borderColor = "transparent";
            bottomDiv[i].style.color = "#999999";
            bottomDiv[i].style.fontWeight = "300";
        }
        bottomDiv[1].style.borderColor = "#910000";
        bottomDiv[1].style.color = "#910000";
        bottomDiv[1].style.fontWeight = "400";
    }
}

function onClickWalletHeaderEvent(selector){
    $(".bottomTable-th-inner").removeClass("activeOrder");
    $(selector).children(".bottomTable-th-inner").addClass("activeOrder");
    $(".bottomTable-th-inner img").attr("src", "img/new/btn_none_order.png");

    if($(selector).find("img").hasClass("asc")) {
        $(selector).find("img").removeClass("asc");
        $(selector).find("img").attr("src", "img/new/btn_desc_order.png");
    } else {
        $(".bottomTable-th-inner").find("img").removeClass("asc");
        $(selector).find("img").addClass("asc");
        $(selector).find("img").attr("src", "img/new/btn_asc_order.png");
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
            $('.listFold').eq(i).css("display", "none");
            $('.listUnfold').eq(i).css("display", "inline-block");
            $('.walletSelectedImg').eq(i).css("display", "none");
            $('.walletUnselectedImg').eq(i).css("display", "inline-block");
            $('.walletName').eq(i).css("font-weight", "300");
            checkedImg[i].style.display = "none";
            uncheckedImg[i].style.display = "inline-block";
            selectedBorder[i].style.borderColor = "transparent";
        }

        $('.listUnfold').eq(0).css("display", "none");
        $('.listFold').eq(0).css("display", "inline-block");
        $('.walletUnselectedImg').eq(0).css("display", "none");
        $('.walletSelectedImg').eq(0).css("display", "inline-block");
        $('.walletName').eq(0).css("font-weight", "400");
        checkedImg[0].style.display = "block";
        uncheckedImg[0].style.display = "none";
        selectedBorder[0].style.borderColor = "#97222F";
        walletDetailsAll[0].style.display = "table-row-group";

        // For Box Shadowing
        $(".walletDetailsAll tr:first-child td").each(function(){
            $(this).css("boxShadow", "inset 0px 11px 8px -10px rgba(0, 0, 0, 0.2)");
        });

        walletList[walletList.length-1].style.borderBottom = "none";
        if(0 == walletList.length-1) {
            walletList[walletList.length-1].style.borderBottom = "1px solid #d8d8d8";
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

    for(let k=0; k<walletList.length; k++) {
        $('.listUnfold').eq(k).click(function() {
            uncheckedImg[k].click();
        });
    }

    for(let k=0; k<walletList.length; k++) {
        $('.listFold').eq(k).click(function() {
            checkedImg[k].click();
        });
    }

    // Copy Modal
    for(let i=0; i<walletList.length; i++) {
        $('.copyRed').eq(i).click(function() {
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
        });
    }

    copyConfirm.onclick = function() {
        copyAddrContent[0].style.display = "none";
        copyAddrModal[0].style.display = "none";
    }

    for(var i=0; i<checkedImg.length; i++){
        checkedImg[i].style.display = "none";
        uncheckedImg[i].style.display = "block";
        selectedBorder[i].style.borderColor = "transparent";
        walletDetailsAll[i].style.display = "none";
    }

    walletList[walletList.length-1].style.borderBottom = "none";

}

function uiInitMainBottomNavi(){
    $('.bottomNeviImg').eq(0).hover(function() {
        $('.bottomNeviImg').eq(0).attr("src", "img/new/btn_changeWalletName_mouseHover.png");
    },
    function() {
        $('.bottomNeviImg').eq(0).attr("src", "img/new/btn_changeWalletName.png");
    });

    $('.bottomNeviImg').eq(1).hover(function() {
        $('.bottomNeviImg').eq(1).attr("src", "img/new/btn_changeWalletPassword_mouseHover.png");
    },
    function() {
        $('.bottomNeviImg').eq(1).attr("src", "img/new/btn_changeWalletPassword.png");
    });

    $('.bottomNeviImg').eq(2).hover(function() {
        $('.bottomNeviImg').eq(2).attr("src", "img/new/btn_backUpWallet_mouseHover.png");
    },
    function() {
        $('.bottomNeviImg').eq(2).attr("src", "img/new/btn_backUpWallet.png");
    });

    $('.bottomNeviImg').eq(3).hover(function() {
        $('.bottomNeviImg').eq(3).attr("src", "img/new/btn_deleteWallet_mouseHover.png");
    },
    function() {
        $('.bottomNeviImg').eq(3).attr("src", "img/new/btn_deleteWallet.png");
    });
}

function uiInitMainCopyAddr() {
    $('.walletAddress').each(function(i) {
        $('.walletList td:nth-child(3)').eq(i).hover(function() {
            $(".walletAddress").eq(i).css("color", "#2b2b2b");
            $(".walletAddress").eq(i).css("text-decoration", "underline");
            $(".copyGrey").eq(i).css("display", "inline-block");
        }, function() {
            $(".walletAddress").eq(i).css("color", "#999999");
            $(".walletAddress").eq(i).css("text-decoration", "none");
            $(".copyAddrBtn").css("display", "none");
        });

        $('.copyGrey').eq(i).hover(function() {
            $(this).css("display", "none");
            $('.copyRed').eq(i).css("display", "inline-block");
        });
        $('.copyRed').eq(i).mouseleave(function() {
            $(this).css("display", "none");
            $('.copyGrey').eq(i).css("display", "inline-block");
        });
    });

}

function uiInitMainChangeName() {
    let changeName;
    let crossBlackFlag = 0;

    $("#change").click(function() {
        $(".changeNameBody input").val("");
        $("#changeNmSpan").click();
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $(".changeNameBody input").css("border-color", "#000000");
        $("#checkGreen").css("display", "none");
        $(".change_name_check").css("opacity", "0");
        $("#changeNmSpan").css("background-color", "#e3e5e9");
        $("#changeNmSpan").css("pointer", "default");

        $(".changeNameModal").css("display", "block");
        $(".changeNameContent").css("display", "block");
    });

    $("#changeNmSpan").click(function() {
        changeName = $(".changeNameBody input").val();

        if(changeName == null || changeName == "") {
            $("#checkGreen").css("display", "none");
            $(".change_name_check").html('<font class="wrong">&times;</font>&nbsp; Enter new wallet name.');
            $(".change_name_check").css("bottom", "5px");
            $(".change_name_check").css("opacity", "1");
            $(".change_name_check").css("color", "#910000");
            $(".changeNameBody input").css("color", "#2b2b2b");
            $(".changeNameBody input").css("border-color", "#910000");
            $('.changeNameBtn .check').css("display", "none");
            $(".changeNameBtn .crossBlack").css("display", "none");
            $(".changeNameBtn .crossRed").css("display", "block");
            $("#changeNmSpan").css("background-color", "#e3e5e9");
            $("#changeNmSpan").css("cursor", "default");
        // confirm Wallet Name existence(**not applied yet)
        } else if(changeName == "q") {
            $("#checkGreen").css("display", "none");
            $(".change_name_check").html('<font class="wrong">&times;</font>&nbsp; This wallet name already exists.');
            $(".change_name_check").css("bottom", "5px");
            $(".change_name_check").css("opacity", "1");
            $(".change_name_check").css("color", "#910000");
            $(".changeNameBody input").css("color", "#2b2b2b");
            $(".changeNameBody input").css("border-color", "#910000");
            $('.changeNameBtn .check').css("display", "none");
            $(".changeNameBtn .crossBlack").css("display", "none");
            $(".changeNameBtn .crossRed").css("display", "block");
            $("#changeNmSpan").css("background-color", "#e3e5e9");
            $("#changeNmSpan").css("cursor", "default");
        } else {
            $(".changeNameModal").css("display", "none");
            $(".changeNameContent").css("display", "none");
        }
    });

    $(".changeNameContent div:nth-child(1)").click(function() {
        $(".changeNameModal").css("display", "none");
        $(".changeNameContent").css("display", "none");
    });

    $(".changeNameBody input").focus(function() {
        $(".changeNameBody input").css("color", "#2b2b2b");
        $(".changeNameBody input").css("border-color", "#36b25b");
        $(".changeNameBtn .check").css("display", "none");
        $(".changeNameBtn .crossRed").css("display", "none");
        $(".changeNameBtn .crossBlack").css("display", "block");
    });

    $(".changeNameBody input").focusout(function() {
        if(crossBlackFlag === 1) {
            $(".changeNameBody input").val("");
            crossBlackFlag = 0;
        }

        changeName = $(".changeNameBody input").val();

        if(changeName == null || changeName == "") {
            $("#checkGreen").css("display", "none");
            $(".change_name_check").html('<font class="wrong">&times;</font>&nbsp; Enter new wallet name.');
            $(".change_name_check").css("bottom", "5px");
            $(".change_name_check").css("opacity", "1");
            $(".change_name_check").css("color", "#910000");
            $(".changeNameBody input").css("border-color", "#910000");
            $('.changeNameBtn .check').css("display", "none");
            $(".changeNameBtn .crossBlack").css("display", "none");
            $(".changeNameBtn .crossRed").css("display", "block");
            $("#changeNmSpan").css("background-color", "#e3e5e9");
            $("#changeNmSpan").css("cursor", "default");
        // confirm Wallet Name existence(**not applied yet)
        } else if(changeName == "q") {
            $("#checkGreen").css("display", "none");
            $(".change_name_check").html('<font class="wrong">&times;</font>&nbsp; This wallet name already exists.');
            $(".change_name_check").css("bottom", "5px");
            $(".change_name_check").css("opacity", "1");
            $(".change_name_check").css("color", "#910000");
            $(".changeNameBody input").css("border-color", "#910000");
            $('.changeNameBtn .check').css("display", "none");
            $(".changeNameBtn .crossBlack").css("display", "none");
            $(".changeNameBtn .crossRed").css("display", "block");
            $("#changeNmSpan").css("background-color", "#e3e5e9");
            $("#changeNmSpan").css("cursor", "default");
        } else {
            $("#checkGreen").css("display", "inline-block");
            $(".change_name_check").html('The wallet name is unregisterd.');
            $(".change_name_check").css("bottom", "0");
            $(".change_name_check").css("opacity", "1");
            $(".change_name_check").css("color", "#36b25b");
            $(".changeNameBody input").css("color", "#999999");
            $(".changeNameBody input").css("borderColor", "#999999");
            $(".changeNameBtn .crossBlack").css("display", "none");
            $(".changeNameBtn .crossRed").css("display", "none");
            $(".changeNameBtn .check").css("display", "block");
            $("#changeNmSpan").css("background-color", "#910000");
            $("#changeNmSpan").css("cursor", "pointer");
        }
    });

    $(".changeNameBtn .crossBlack").mouseenter(function() {
        crossBlackFlag = 1;
    });

    $(".changeNameBtn .crossBlack").mouseleave(function() {
        crossBlackFlag = 0;
    });

    $(".changeNameBtn .crossRed").click(function() {
        $(".changeNameBody input").val("");
    });

}

function uiInitMainChangePassword() {
    let changePassword;
    let crossBlackFlag = 0;

    $("#password").click(function() {
        $(".changePasswordBody input").val("");
        $("#changePwSpan").click();
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $(".changePasswordBody input").css("border-color", "#000000");
        $(".changePasswordBody label[class$='check']").css("opacity", "0");
        $(".changePasswordBody input").attr("type", "password");
        $(".changePasswordContent div[class$='cover'] img:first-child").css("display", "block");
        $(".changePasswordContent div[class$='cover'] img:last-child").css("display", "none");
        $("#changePwSpan").css("background-color", "#e3e5e9");
        $("#changePwSpan").css("pointer", "default");

        $(".changePasswordModal").css("display", "block");
        $(".changePasswordContent").css("display", "block");
    });

    $("#changePwSpan").click(function() {
        let currentPw = $(".changePasswordBody input").eq(0).val();
        let newPw = $(".changePasswordBody input").eq(1).val();
        let confirmPw = $(".changePasswordBody input").eq(2).val();
        let validationFlag = 0;
    
        if(currentPw == null || currentPw == "") {
            $(".change_password_current_check").html('<font class="wrong">&times;</font>&nbsp; Please enter your password.');
            $(".change_password_current_check").css("opacity", "1");
            $(".changePasswordBody input").eq(0).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(0).css("border-color", "#910000");
        // Match the password with Keystore files(**not applied yet)
        } else if(currentPw == "q") {
            $(".change_password_current_check").html('<font class="wrong">&times;</font>&nbsp; Please check your password.');
            $(".change_password_current_check").css("opacity", "1");
            $(".changePasswordBody input").eq(0).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(0).css("border-color", "#910000");
        } else {
            $(".change_password_current_check").css("opacity", "0");
            $(".changePasswordBody input").eq(0).css("color", "#999999");
            $(".changePasswordBody input").eq(0).css("borderColor", "#999999");
            validationFlag++;
        }
    
        changePwValidate = [];
    
        if(newPw.match(letters)) {
            changePwValidate.push(true);
        }
        if(newPw.match(numbers)) {
            changePwValidate.push(true);
        }
        if(newPw.match(specials)) {
            changePwValidate.push(true);
        }
    
        if(newPw == null || newPw == "") {
            $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Please enter your password.');
            $(".change_password_new_check").css("opacity", "1");
            $(".changePasswordBody input").eq(1).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(1).css("border-color", "#910000");
        } else if(newPw.length < 8) { 
            $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.');
            $(".change_password_new_check").css("opacity", "1");
            $(".changePasswordBody input").eq(1).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(1).css("border-color", "#910000");
        } else if(changePwValidate.length !== 3) {
            $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Password must contain a combination of letters, numbers, and special characters.');
            $(".change_password_new_check").css("opacity", "1");
            $(".changePasswordBody input").eq(1).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(1).css("border-color", "#910000");
        } else {
            $(".change_password_new_check").css("opacity", "0");
            $(".changePasswordBody input").eq(1).css("color", "#999999");
            $(".changePasswordBody input").eq(1).css("borderColor", "#999999");
            validationFlag++;
        }
    
        if(confirmPw == null || confirmPw == "") {
            $(".change_password_confirm_check").html('<font class="wrong">&times;</font>&nbsp; Please check your password.');
            $(".change_password_confirm_check").css("opacity", "1");
            $(".changePasswordBody input").eq(2).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(2).css("border-color", "#910000");
        } else if(newPw !== confirmPw) { 
            $(".change_password_confirm_check").html('<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.');
            $(".change_password_confirm_check").css("opacity", "1");
            $(".changePasswordBody input").eq(2).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(2).css("border-color", "#910000");
        } else {
            $(".change_password_confirm_check").css("opacity", "0");
            $(".changePasswordBody input").eq(2).css("color", "#999999");
            $(".changePasswordBody input").eq(2).css("borderColor", "#999999");
            validationFlag++;
        }
    
        if(validationFlag === 3) {
            $(".changePasswordModal").css("display", "none");
            $(".changePasswordContent").css("display", "none");
        }
    });

    $(".changePasswordContent div:nth-child(1)").click(function() {
        $(".changePasswordModal").css("display", "none");
        $(".changePasswordContent").css("display", "none");
    });

    $(".changePasswordBody input").each(function(index) {
        $(".changePasswordBody input").eq(index).focus(function() {
            $(".changePasswordBody input").eq(index).css("color", "#2b2b2b");
            $(".changePasswordBody input").eq(index).css("border-color", "#36b25b");
            $(".changePasswordContent .check").eq(index).css("display", "none");
            $(".changePasswordContent .crossRed").eq(index).css("display", "none");
            $(".changePasswordContent .crossBlack").eq(index).css("display", "block");
        });

        $(".changePasswordBody input").eq(index).focusout(function() {
            if(crossBlackFlag === 1) {
                $(".changePasswordBody input").eq(index).val("");
                crossBlackFlag = 0;
            }
    
            changePassword = $(".changePasswordBody input").eq(index).val();
    
            if(index == 0) {
                if(changePassword == null || changePassword == "") {
                    $(".change_password_current_check").html('<font class="wrong">&times;</font>&nbsp; Please enter your password.');
                    $(".change_password_current_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwCurrentBtn .check').css("display", "none");
                    $(".changePwCurrentBtn .crossBlack").css("display", "none");
                    $(".changePwCurrentBtn .crossRed").css("display", "block");
                // Match the password with Keystore files(**not applied yet)
                } else if(changePassword == "q") {
                    $(".change_password_current_check").html('<font class="wrong">&times;</font>&nbsp; Please check your password.');
                    $(".change_password_current_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwCurrentBtn .check').css("display", "none");
                    $(".changePwCurrentBtn .crossBlack").css("display", "none");
                    $(".changePwCurrentBtn .crossRed").css("display", "block");
                } else {
                    $(".change_password_current_check").css("opacity", "0");
                    $(".changePasswordBody input").eq(index).css("color", "#999999");
                    $(".changePasswordBody input").eq(index).css("borderColor", "#999999");
                    $(".changePwCurrentBtn .crossBlack").css("display", "none");
                    $(".changePwCurrentBtn .crossRed").css("display", "none");
                    $(".changePwCurrentBtn .check").css("display", "block");
                }
            } else if(index == 1) {
                changePwValidate = [];

                if(changePassword.match(letters)) {
                    changePwValidate.push(true);
                }
                if(changePassword.match(numbers)) {
                    changePwValidate.push(true);
                }
                if(changePassword.match(specials)) {
                    changePwValidate.push(true);
                }

                if(changePassword == null || changePassword == "") {
                    $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Please enter your password.');
                    $(".change_password_new_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwNewBtn .check').css("display", "none");
                    $(".changePwNewBtn .crossBlack").css("display", "none");
                    $(".changePwNewBtn .crossRed").css("display", "block");
                } else if(changePassword.length < 8) { 
                    $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.');
                    $(".change_password_new_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwNewBtn .check').css("display", "none");
                    $(".changePwNewBtn .crossBlack").css("display", "none");
                    $(".changePwNewBtn .crossRed").css("display", "block");
                } else if(changePwValidate.length !== 3) {
                    $(".change_password_new_check").html('<font class="wrong">&times;</font>&nbsp; Password must contain a combination of letters, numbers, and special characters.');
                    $(".change_password_new_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwNewBtn .check').css("display", "none");
                    $(".changePwNewBtn .crossBlack").css("display", "none");
                    $(".changePwNewBtn .crossRed").css("display", "block");
                } else {
                    $(".change_password_new_check").css("opacity", "0");
                    $(".changePasswordBody input").eq(index).css("color", "#999999");
                    $(".changePasswordBody input").eq(index).css("borderColor", "#999999");
                    $(".changePwNewBtn .crossBlack").css("display", "none");
                    $(".changePwNewBtn .crossRed").css("display", "none");
                    $(".changePwNewBtn .check").css("display", "block");
                }

                if(!($(".changePasswordBody input").eq(2).val() === null
                    || $(".changePasswordBody input").eq(2).val() === ""
                    || $(".changePasswordBody input").eq(2).val().length == 0)) {
                    $(".changePasswordBody input").eq(2).focusout();
                }
            } else if(index == 2) {
                if(changePassword == null || changePassword == "") {
                    $(".change_password_confirm_check").html('<font class="wrong">&times;</font>&nbsp; Please check your password.');
                    $(".change_password_confirm_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwConfirmBtn .check').css("display", "none");
                    $(".changePwConfirmBtn .crossBlack").css("display", "none");
                    $(".changePwConfirmBtn .crossRed").css("display", "block");
                } else if($(".changePasswordBody input").eq(1).val() !== changePassword) { 
                    $(".change_password_confirm_check").html('<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.');
                    $(".change_password_confirm_check").css("opacity", "1");
                    $(".changePasswordBody input").eq(index).css("border-color", "#910000");
                    $('.changePwConfirmBtn .check').css("display", "none");
                    $(".changePwConfirmBtn .crossBlack").css("display", "none");
                    $(".changePwConfirmBtn .crossRed").css("display", "block");
                } else {
                    $(".change_password_confirm_check").css("opacity", "0");
                    $(".changePasswordBody input").eq(index).css("color", "#999999");
                    $(".changePasswordBody input").eq(index).css("borderColor", "#999999");
                    $(".changePwConfirmBtn .crossBlack").css("display", "none");
                    $(".changePwConfirmBtn .crossRed").css("display", "none");
                    $(".changePwConfirmBtn .check").css("display", "block");
                }
            }
        });

        $(".changePasswordBody input").eq(index).keyup(function() {
            if(index == 0) {
                activatePwChange("currentPw", "keyUp");
            } else if(index == 1) {
                activatePwChange("newPw", "keyUp");
            } else if(index == 2) {
                activatePwChange("confirmPw", "keyUp");
            }
        });

        $(".changePasswordContent .crossBlack").eq(index).mouseenter(function() {
            crossBlackFlag = 1;
        });
    
        $(".changePasswordContent .crossBlack").eq(index).mouseleave(function() {
            crossBlackFlag = 0;
        });
    
        $(".changePasswordContent .crossRed").eq(index).click(function() {
            $(".changePasswordBody input").eq(index).val("");
        });
    });

    function activatePwChange(inputId, eventNm) {
        let currentPw = $(".changePasswordBody input").eq(0).val();
        let newPw = $(".changePasswordBody input").eq(1).val();
        let confirmPw = $(".changePasswordBody input").eq(2).val();
        let validationFlag = 0;
    
        if(currentPw == null || currentPw == "") {
         // Match the password with Keystore files(**not applied yet)
        } else if(currentPw == "q") {
        } else {
            validationFlag++;
        }
    
        changePwValidate = [];
    
        if(newPw.match(letters)) {
            changePwValidate.push(true);
        }
        if(newPw.match(numbers)) {
            changePwValidate.push(true);
        }
        if(newPw.match(specials)) {
            changePwValidate.push(true);
        }
    
        if(newPw == null || newPw == "") {
        } else if(newPw.length < 8) {
        } else if(changePwValidate.length !== 3) {
        } else {
            validationFlag++;
        }
    
        if(confirmPw == null || confirmPw == "") {
        } else if(newPw !== confirmPw) {
        } else {
            validationFlag++;
        }
    
        if(validationFlag === 3) {
            $('.check').css("display", "block");
            $('.crossBlack').css("display", "none");
            $('.crossRed').css("display", "none");
            $(".changePasswordBody input").css("color", "#999999");
            $(".changePasswordBody input").css("border-color", "#999999");
            $(".changePasswordBody .warn").css("opacity", "0");
            $("#changePwSpan").css("background-color", "#910000");
            $("#changePwSpan").css("cursor", "pointer");
    
            if(eventNm === "keyUp") {
                if(inputId == "currentPw") {
                    $(".changePasswordBody input").eq(0).focus();
                } else if(inputId == "newPw") {
                    $(".changePasswordBody input").eq(1).focus();
                } else if(inputId == "confirmPw") {
                    $(".changePasswordBody input").eq(2).focus();
                }
            }
        } else {
            $("#changePwSpan").css("background-color", "#e3e5e9");
            $("#changePwSpan").css("cursor", "default");
        }
    }

    $(".change_password_current_cover img:nth-child(1)").click(function() {
        $(".changePasswordBody input").eq(0).attr("type", "text");
        $(".change_password_current_cover img:nth-child(1)").css("display", "none");
        $(".change_password_current_cover img:nth-child(2)").css("display", "block");
    });

    $(".change_password_current_cover img:nth-child(2)").click(function() {
        $(".changePasswordBody input").eq(0).attr("type", "password");
        $(".change_password_current_cover img:nth-child(2)").css("display", "none");
        $(".change_password_current_cover img:nth-child(1)").css("display", "block");
    });

    $(".change_password_new_cover img:nth-child(1)").click(function() {
        $(".changePasswordBody input").eq(1).attr("type", "text");
        $(".change_password_new_cover img:nth-child(1)").css("display", "none");
        $(".change_password_new_cover img:nth-child(2)").css("display", "block");
    });

    $(".change_password_new_cover img:nth-child(2)").click(function() {
        $(".changePasswordBody input").eq(1).attr("type", "password");
        $(".change_password_new_cover img:nth-child(2)").css("display", "none");
        $(".change_password_new_cover img:nth-child(1)").css("display", "block");
    });

    $(".change_password_confirm_cover img:nth-child(1)").click(function() {
        $(".changePasswordBody input").eq(2).attr("type", "text");
        $(".change_password_confirm_cover img:nth-child(1)").css("display", "none");
        $(".change_password_confirm_cover img:nth-child(2)").css("display", "block");
    });

    $(".change_password_confirm_cover img:nth-child(2)").click(function() {
        $(".changePasswordBody input").eq(2).attr("type", "password");
        $(".change_password_confirm_cover img:nth-child(2)").css("display", "none");
        $(".change_password_confirm_cover img:nth-child(1)").css("display", "block");
    });
}

function uiInitMainBackupWallet(){
    $("#backUp").click(function() {
        $(".backupWalletBody input").css("color", "#999999");
        $(".backupWalletBody input").css("border-color", "#999999");
        $(".backupWalletBody input").attr("type", "password");
        $(".backup_wallet_pk_cover img:nth-child(2)").css("display", "none");
        $(".backup_wallet_pk_cover img:nth-child(1)").css("display", "block");

        $(".backupWalletModal").css("display", "block");
        $(".backupWalletContent").css("display", "block");
    });

    $(".backupWalletContent div:nth-child(1)").click(function() {
        $(".backupWalletModal").css("display", "none");
        $(".backupWalletContent").css("display", "none");
    });


    $('.backup_wallet_pk_copy img').click(function() {
        // copy_complete[0].style.display = "block";
        let private_key_copy = document.createElement("INPUT");
        private_key_copy.setAttribute("type", "text");
        // Backup Private Key load(**not applied yet)
        private_key_copy.setAttribute("value", $(".backupWalletBody input").val());
        document.body.appendChild(private_key_copy);
        private_key_copy.select();
        document.execCommand('copy');
        document.body.removeChild(private_key_copy);
        // Copy Complete Timer
        // setTimeout(function() {copy_complete[0].style.display = "none"}, 2000);
    });

    $(".backup_wallet_pk_cover img:nth-child(1)").click(function() {
        $(".backupWalletBody input").css("color", "#2b2b2b");
        $(".backupWalletBody input").css("border-color", "#2b2b2b");
        $(".backupWalletBody input").attr("type", "text");
        $(".backup_wallet_pk_cover img:nth-child(1)").css("display", "none");
        $(".backup_wallet_pk_cover img:nth-child(2)").css("display", "block");
    });

    $(".backup_wallet_pk_cover img:nth-child(2)").click(function() {
        $(".backupWalletBody input").css("color", "#999999");
        $(".backupWalletBody input").css("border-color", "#999999");
        $(".backupWalletBody input").attr("type", "password");
        $(".backup_wallet_pk_cover img:nth-child(2)").css("display", "none");
        $(".backup_wallet_pk_cover img:nth-child(1)").css("display", "block");
    });
}

function uiInitMainRemoveWallet() {
    $("#delete").click(function() {
        $(".removeWalletModal").css("display", "block");
        $(".removeWalletContent").css("display", "block");
    });

    $(".removeWalletBtnDiv span:first-child").click(function() {
        $(".removeWalletModal").css("display", "none");
        $(".removeWalletContent").css("display", "none");
    });

    $(".removeWalletBtnDiv span:last-child").click(function() {
        $(".removeWalletModal").css("display", "none");
        $(".removeWalletContent").css("display", "none");
    });    

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

    $('.listUnfold').eq(index).css("display", "none");
    $('.listFold').eq(index).css("display", "inline-block");
    $('.walletUnselectedImg').eq(index).css("display", "none");
    $('.walletSelectedImg').eq(index).css("display", "inline-block");
    $('.walletName').eq(index).css("font-weight", "400");
    walletList[index].style.background = "#f2f2f2";
    checkedImg[index].style.display = "inline-block";
    uncheckedImg[index].style.display = "none";
    selectedBorder[index].style.borderColor = "#97222F";
    walletDetailsAll[index].style.display = "table-row-group";
}

function walletUnCheck(index){
    mainCheckListIndex = index;

    // Wallet List
    const walletList = document.querySelectorAll('.walletList');
    const walletDetailsAll = document.querySelectorAll('.walletDetailsAll');
    const checkedImg = document.querySelectorAll('#checkedImg');
    const uncheckedImg = document.querySelectorAll('#uncheckedImg');
    const selectedBorder = document.querySelectorAll('.walletList td:first-child');

    $('.listFold').eq(index).css("display", "none");
    $('.listUnfold').eq(index).css("display", "inline-block");
    $('.walletSelectedImg').eq(index).css("display", "none");
    $('.walletUnselectedImg').eq(index).css("display", "inline-block");
    $('.walletName').eq(index).css("font-weight", "300");
    walletList[index].style.background = "#ffffff";
    checkedImg[index].style.display = "none";
    uncheckedImg[index].style.display = "inline-block";
    selectedBorder[index].style.borderColor = "transparent";
    walletDetailsAll[index].style.display = "none";

    walletList[walletList.length-1].style.borderBottom = "1px solid #d8d8d8";
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
        $('.listFold').eq(i).css("display", "none");
        $('.listUnfold').eq(i).css("display", "inline-block");
        $('.walletSelectedImg').eq(i).css("display", "none");
        $('.walletUnselectedImg').eq(i).css("display", "inline-block");
        $('.walletName').eq(i).css("font-weight", "300");
        walletList[i].style.background = "#ffffff";
        checkedImg[i].style.display = "none";
        uncheckedImg[i].style.display = "inline-block";
        selectedBorder[i].style.borderColor = "transparent";
        walletDetailsAll[i].style.display = "none";
    }

}
function setTotalBalance(balance){
    if (typeof(balance) != "undefined" && $(balance) != null){
        balance = addDotWidthIndex(balance);
        $("#APISNum font").eq(0).text(balance.split(".")[0]+".");
        $("#APISNum font").eq(1).text(balance.split(".")[1]);
    }
}

function setTotalMineral(mineral){

    mineral = addDotWidthIndex(mineral);

    $("#amountMnr font").eq(0).text(mineral.split(".")[0]+".");
    $("#amountMnr font").eq(1).text(mineral.split(".")[1]);
}




/* ==================================================
 * transfer - init ui control
 * ================================================== */
 function uiInitTransfer(){
    //wallet select
    $(".transfer-article-body .box .select-selected").eq(0).on("click",function(){
        $(this).eq(0).parent().parent().addClass("active");
    });

    //percent select
    $(".transfer-article-body .box .select-selected").eq(1).on("click",function(){
        $(this).eq(1).parent().parent().parent().parent().addClass("active");

        $(".transfer-article-body .box .select-selected").eq(0).parent().parent().removeClass("active");
    });
    $(".transfer-article-body .box .select-selected").eq(1).focusout(function(){
        $(this).eq(1).parent().parent().parent().parent().removeClass("active");
    });

    $(".transfer-article-body .receving-address input").on("click",function(){
        $(this).parent().parent().addClass("active");
    });
    $(".transfer-article-body .receving-address input").focusout(function(){
        $(this).parent().parent().removeClass("active");

        var value = $(this).val();
        setTransferAmountToAddress(value);

        value = (value) ? value : "Write Receving Address";
        $(".transfer-article-body .receving-address .placeholder").text(value);
    });

    $(".transfer-article-body .box .placeholder").each(function(idx){

        if(idx == 0){
            $(this).on("click",function(){
                $(this).hide();
                $(".transfer-article-body .box .text").eq(0).show();
                $(this).parent().parent().parent().addClass("active");
                $(".transfer-article-body .box .select-selected").parent().parent().removeClass("active");
            });
        }else if(idx == 1){
            $(this).on("click",function(){
                $(this).hide();
                $(".transfer-article-body .box .text").eq(1).show();
                $(this).parent().parent().addClass("active");
                $(".transfer-article-body .box .select-selected").parent().parent().removeClass("active");
            });
        }

    });

    $(".transfer-article-body .box .text").each(function(idx){
        if(idx == 0){
            $(this).focusout(function(){
                $(this).hide();
                $(".transfer-article-body .box .placeholder").eq(0).show();
                $(this).parent().parent().parent().removeClass("active");

                var value = $(".transfer-article-body .box .text input").eq(0).val();
                value = (value) ? value : "0.0";
                value = value.replace(/[^0-9.]/g,'');
                if(value.indexOf(".") < 0){
                    value = value+".";
                }
                var natural = BigInteger(value.split(".")[0]).toString();
                var decimal = appendTextZero(value.split(".")[1]);

                $(".transfer-article-body .box .text input").eq(0).val(natural+"."+decimal);
                $(".transfer-article-body .box .placeholder .natural").html(natural + ".");
                $(".transfer-article-body .box .placeholder .decimal").html(decimal);

                $("#transfer_transfer_amount").val(natural+decimal);
                settingTransferData();
            });
        }else if(idx == 1){
            $(this).focusout(function(){
                $(this).hide();
                $(".transfer-article-body .box .placeholder").eq(1).show();
                $(this).parent().parent().removeClass("active");

            });
        }
    });

    $(".bar-handle").draggable({
        axis : "x",
        containment : ".bar",
        drag: function( event, ui){
            var left = ui.position.left + $(".bar-handle").width()/2;
            var width = $(".bar").width() - $(".bar-handle").outerWidth();
            var r = ui.position.left / width;
            r = r * 100;

            var minGasPrice = BigInteger("50000000000");
            var maxGasPrice = BigInteger("500000000000");
            var gasPrice = BigInteger(minGasPrice).add( maxGasPrice.subtract(minGasPrice).multiply(r).divide("100"));
            $("#transfer_gas_price").val(gasPrice);

            gasPrice = gasPrice.multiply("200000");
            gasPrice = addDotWidthIndex(gasPrice.toString());
            var natural = gasPrice.split(".")[0];
            var decimal = gasPrice.split(".")[1];

            $(".gas-price-slider .natural").text(natural+".");
            $(".gas-price-slider .decimal").text(decimal);

            $(".bar-move").css("width",left+"px");
            $(".bar input[type='hidden']").val(r);

            settingTransferData();
        }
    });
 }

/* ==================================================
 * transfer - method ui control
 * ================================================== */



/* ==================================================
 * start javascript
 * ================================================== */
function ui_start(){
    uiInitStatus();
    uiInitHeaderDiv();
}