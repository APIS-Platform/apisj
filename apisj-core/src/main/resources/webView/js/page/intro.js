let fileValidationFlag = "FileException";


function dragAndDropOpenFileReader(fileName, flag){
    fileValidationFlag = flag;
    let keystoreFileName = document.getElementsByClassName("keystore-file-name");
    let keystoreFileNameFont = document.getElementById('keystore-file-name');
    let fileFormCheck = document.getElementById('file_form_check');

    if(fileValidationFlag == "CorrectFileForm") {
        keystoreFileNameFont.innerHTML = fileName;
        keystoreFileName[0].style.display = "block";
        keystoreFileName[0].style.background = "#999999";
        fileFormCheck.style.opacity = 0;
    } else if(fileValidationFlag == "IncorrectFileForm") {
        keystoreFileNameFont.innerHTML = fileName;
        keystoreFileName[0].style.display = "block";
        keystoreFileName[0].style.background = "#910000";
        fileFormCheck.style.opacity = 1;
    } else if(fileValidationFlag == "FileException") {
        keystoreFileName[0].style.display = "none";
        fileFormCheck.style.opacity = 0;
    } else {
        app.errorPopup();
    }
}


  function intro_start(){

    // Create wallet modal
    let create_modal = document.getElementById('create-wallet-modal');
    let create_wallet_btn = document.getElementById('create-wallet-btn');
    let phase_next = document.querySelectorAll('.phase-next');
    let phase_back = document.querySelectorAll('.phase-back');
    let create_modal_content = document.querySelectorAll('.create-modal-content');
    let wallet_name_tf = document.querySelectorAll(".wallet_name");
    let wallet_password_pw = document.querySelectorAll(".wallet_password");
    let password_confirm_pw = document.querySelectorAll(".password_confirm");
    let name_check = document.querySelectorAll(".name_check");
    let password_check = document.querySelectorAll(".password_check");
    let conf_password_check = document.querySelectorAll(".conf_password_check");
    let crossBlackFlag = 0;
    // For validating values
    let name = wallet_name_tf[0].value;
    let password = wallet_password_pw[0].value;
    let conf_password = password_confirm_pw[0].value;
    let password_validate = [];
    let letters = /[a-zA-Z]/g;
    let numbers = /[0-9]/g;
    let specials = /[^(a-zA-Z0-9)]/g;
    let privateKey_validate = /[^(0-9a-fA-F)]/g;
    // Keystore
    let download_keystore = document.getElementsByClassName('download-keystore');
    let download_keystore_flag = 0;
    let option_modal = document.querySelectorAll('#option-modal');
    let modal_option_content = document.querySelectorAll('.modal-option-content');
    // Private Key
    let private_key = document.getElementById("private-key");
    let private_key_cover_img = document.getElementById("key-cover");
    let private_key_uncover_img = document.getElementById("key-uncover");

    // Load wallet modal
    let load_wallet_btn = document.getElementById('load-wallet-btn');
    let load_modal_content = document.querySelectorAll('.load-modal-content');
    let load_radio = document.getElementsByName("load-wallet-radio");
    let load_password_pw = document.getElementById("load_wallet_password");
    let load_password_check = document.getElementById("load_password_check");
    let load_password = load_password_pw.value;
    let load_privateKey = $('#load_wallet_privateKey').val();
    let select_keystore = document.getElementsByClassName("select-keystore");
    let file_form_check = document.getElementById('file_form_check');
    let close_file = document.getElementsByClassName("close_file");
    let keystore_file_name = document.getElementsByClassName("keystore-file-name");
    let keystore_file_name_font = document.getElementById('keystore-file-name');
    let drag_and_drop = document.getElementsByClassName("drag-and-drop");

    // Create & Load Wallet mouseover
    create_wallet_btn.onmouseover = function() {
        create_wallet_btn.style.background = "url('img/new/btn_create_wallet.png') no-repeat center";
        load_wallet_btn.style.background = "url('img/new/btn_load_wallet_none.png') no-repeat center";
        $(".introGroupDetails:nth-child(2) div:nth-child(2) font").css("opacity","1");
        $(".introGroupDetails:nth-child(2) div:nth-child(3) font").css("opacity","0.3");
        $("#plusWhite").attr("src","img/new/icon_plus_white.png");
        $("#downArrowWhite").attr("src","img/new/icon_down_arrow_white_none.png");
        $(".introGroupIndex p:nth-child(5)").css("opacity", "0.5");
        $("#navSpan img:nth-child(4)").css("display", "inline");
    }
    load_wallet_btn.onmouseover = function() {
        create_wallet_btn.style.background = "url('img/new/btn_create_wallet_none.png') no-repeat center";
        load_wallet_btn.style.background = "url('img/new/btn_load_wallet.png') no-repeat center";
        $(".introGroupDetails:nth-child(2) div:nth-child(2) font").css("opacity","0.3");
        $(".introGroupDetails:nth-child(2) div:nth-child(3) font").css("opacity","1");
        $("#plusWhite").attr("src","img/new/icon_plus_white_none.png");
        $("#downArrowWhite").attr("src","img/new/icon_down_arrow_white.png");
        $(".introGroupIndex p:nth-child(5)").css("opacity", "0");
        $("#navSpan img:nth-child(4)").css("display", "none");
    }
    $('.introGroupDetails:nth-child(2) div:nth-child(2)').mouseover(function() {
        $('#create-wallet-btn').mouseover();
    });
    $('.introGroupDetails:nth-child(2) div:nth-child(3)').mouseover(function() {
        $('#load-wallet-btn').mouseover();
    });

    // Create wallet function
    create_wallet_btn.onclick = function() {
        createPhase2();
        // Reset values
        wallet_name_tf[0].value = '';
        wallet_password_pw[0].value = '';
        password_confirm_pw[0].value = '';
        // Init margin Setting
        phase_next[0].click();
        name_check[0].style.opacity = 0;
        password_check[0].style.opacity = 0;
        conf_password_check[0].style.opacity = 0;
        wallet_name_tf[0].style.borderColor = "#000000";
        wallet_password_pw[0].style.borderColor = "#000000";
        password_confirm_pw[0].style.borderColor = "#000000";
        $('.next').attr("src", "img/new/btn_next_none.png");
        $('.phase-next').css("cursor", "default");
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $('.progressPhases').css("display", "block");
        create_modal_content[0].style.display = "block";
        crossBlackFlag = 0;
        download_keystore_flag = 0;
        app.resetKeystore();
    }

    $('.introGroupDetails:nth-child(2) div:nth-child(2)').click(function() {
        create_wallet_btn.click();
    });

    // Intro Group Change
    function createPhase2() {
        $(".introGroupIndex p:nth-child(1)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(2)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(2)").text("02");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(2)").css("display", "none");
        $(".introGroupDetails:nth-child(3)").css("display", "block");
        $("#navSpan img:nth-child(1)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav.png");
        $(".wallet_password").eq(0).attr("type", "password");
        $(".password_confirm").eq(0).attr("type", "password");
        $(".pwCover").eq(0).css("display", "block");
        $(".pwUncover").eq(0).css("display", "none");
        $(".confirmPwCover").eq(0).css("display", "block");
        $(".confirmPwUncover").eq(0).css("display", "none");
    }

    function backCreatePhase2() {
        $(".introGroupIndex p:nth-child(1)").css("opacity","1");
        $(".introGroupIndex p:nth-child(2)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(2)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(3)").text('02');
        $(".introGroupDetails:nth-child(3)").css("display","none");
        $(".introGroupDetails:nth-child(2)").css("display","block");
        $("#navSpan img:nth-child(2)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(1)").attr("src","img/new/icon_nav.png");
    }

    function createPhase3() {
        $(".introGroupIndex p:nth-child(2)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(3)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(3)").text("03");
        $(".introGroupIndex p:nth-child(4)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(3)").css("display", "none");
        $(".introGroupDetails:nth-child(4)").css("display", "block");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav.png");
        $('.next').attr("src", "img/new/btn_next_none.png");
        $('.phase-next').css("cursor", "default");

    }

    function backCreatePhase3() {
        $(".introGroupIndex p:nth-child(2)").css("opacity","1");
        $(".introGroupIndex p:nth-child(3)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(4)").text('03');
        $(".introGroupDetails:nth-child(4)").css("display","none");
        $(".introGroupDetails:nth-child(3)").css("display","block");
        $("#navSpan img:nth-child(3)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src","img/new/icon_nav.png");
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
    }

    function createPhase4() {
        $(".introGroupIndex p:nth-child(3)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(4)").css("opacity","1");
        $(".introGroupIndex p:nth-child(4)").text("04");
        $(".introGroupIndex p:nth-child(5)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(4)").css("display","none");
        $(".introGroupDetails:nth-child(5)").css("display","block");
        $("#navSpan img:nth-child(3)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(4)").attr("src","img/new/icon_nav.png");
        private_key.style.color = "#999999";
        private_key.style.borderColor = "#999999";
        private_key_uncover_img.style.display = "none";
        private_key_cover_img.style.display = "block";
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
    }

    function backCreatePhase4() {
        $(".introGroupIndex p:nth-child(3)").css("opacity","1");
        $(".introGroupIndex p:nth-child(4)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(4)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(5)").text('04');
        $(".introGroupDetails:nth-child(5)").css("display","none");
        $(".introGroupDetails:nth-child(4)").css("display","block");
        $("#navSpan img:nth-child(4)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(3)").attr("src","img/new/icon_nav.png");
        if(download_keystore_flag == 0) {
            $('.next').attr("src","img/new/btn_next_none.png");
            $('.phase-next').css("cursor","default");
        }
    }

    function loadPhase2() {
        $(".introGroupIndex p:nth-child(1)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(2)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(2)").text("02");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(2)").css("display", "none");
        $(".introGroupDetails:nth-child(6)").css("display", "block");
        $("#navSpan img:nth-child(1)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav.png");
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
    }

    function backLoadPhase2() {
        $(".introGroupIndex p:nth-child(1)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(2)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(2)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(3)").text("02");
        $(".introGroupDetails:nth-child(6)").css("display", "none");
        $(".introGroupDetails:nth-child(2)").css("display", "block");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(1)").attr("src", "img/new/icon_nav.png");
        $(".introGroupIndex p:nth-child(5)").css("opacity", "0");
        $("#navSpan img:nth-child(4)").css("display", "none");
    }

    function loadPhase3() {
        $(".introGroupIndex p:nth-child(2)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(3)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(3)").text("03");
        $(".introGroupIndex p:nth-child(4)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(6)").css("display", "none");
        $(".introGroupDetails:nth-child(7)").css("display", "block");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav.png");
        keystore_file_name[0].style.display = "none";
        file_form_check.style.opacity = 0;
        load_password_pw.value = '';
        load_password_check.style.opacity = 0;
        load_password_pw.style.borderColor = "#000000";
        $("#load_wallet_password").attr("type", "password");
        $("#loadPwCover").css("display", "block");
        $("#loadPwUncover").css("display", "none");
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $('.next').attr("src","img/new/btn_load_grey.png");
        $('.phase-next').css("cursor", "default");

        app.setVisibleDragAndDropPanel(true);
    }

    function backLoadPhase3() {
        $(".introGroupIndex p:nth-child(2)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(3)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(4)").text("03");
        $(".introGroupDetails:nth-child(7)").css("display", "none");
        $(".introGroupDetails:nth-child(6)").css("display", "block");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav.png");
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");

        app.setVisibleDragAndDropPanel(false);
    }

    function loadPhase4() {
        $(".introGroupIndex p:nth-child(2)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(3)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(3)").text("03");
        $(".introGroupIndex p:nth-child(4)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(6)").css("display", "none");
        $(".introGroupDetails:nth-child(8)").css("display", "block");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav.png");
        $('#load_wallet_privateKey').val('');
        $('#load_privateKey_check').css('opacity', '0');
        $('#load_wallet_privateKey').css('borderColor', '#000000');
        $("#load_wallet_privateKey").attr("type", "password");
        $("#loadPkCover").css("display", "block");
        $("#loadPkUncover").css("display", "none");
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $('.next').attr("src","img/new/btn_next_none.png");
        $('.phase-next').css("cursor", "default");
    }

    function backLoadPhase4() {
        $(".introGroupIndex p:nth-child(2)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(3)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(4)").text("03");
        $(".introGroupDetails:nth-child(8)").css("display", "none");
        $(".introGroupDetails:nth-child(6)").css("display", "block");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src", "img/new/icon_nav.png");
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
    }

    function loadPhase5() {
        $(".introGroupIndex p:nth-child(3)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(4)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(4)").text("04");
        $(".introGroupIndex p:nth-child(5)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(8)").css("display", "none");
        $(".introGroupDetails:nth-child(9)").css("display", "block");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(4)").attr("src", "img/new/icon_nav.png");
        $(".wallet_password").eq(1).attr("type", "password");
        $(".password_confirm").eq(1).attr("type", "password");
        $(".pwCover").eq(1).css("display", "block");
        $(".pwUncover").eq(1).css("display", "none");
        $(".confirmPwCover").eq(1).css("display", "block");
        $(".confirmPwUncover").eq(1).css("display", "none");
        crossBlackFlag = 0;
        $('.next').attr("src","img/new/btn_load_grey.png");
        $('.phase-next').css("cursor", "default");
    }

    function backLoadPhase5() {
        $(".introGroupIndex p:nth-child(3)").css("opacity", "1");
        $(".introGroupIndex p:nth-child(4)").css("opacity", "0.5");
        $(".introGroupIndex p:nth-child(4)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(5)").text("04");
        $(".introGroupDetails:nth-child(9)").css("display", "none");
        $(".introGroupDetails:nth-child(8)").css("display", "block");
        $("#navSpan img:nth-child(4)").attr("src", "img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(3)").attr("src", "img/new/icon_nav.png");
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
    }

    wallet_name_tf[0].onfocus = function() {
        wallet_name_tf[0].style.color = "#2b2b2b";
        wallet_name_tf[0].style.borderColor = "#36b25b";
        $(".check").eq(0).css("display", "none");
        $(".crossRed").eq(0).css("display", "none");
        $(".crossBlack").eq(0).css("display", "block");
    }
    wallet_name_tf[0].onfocusout = function() {
        if(crossBlackFlag === 1) {
            wallet_name_tf[0].value = "";
            crossBlackFlag = 0;
        }

        name = wallet_name_tf[0].value;

        if(name == null || name == "" || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.';
            name_check[0].style.opacity = 1;
            wallet_name_tf[0].style.borderColor = "#910000";
            $('.check').eq(0).css("display", "none");
            $(".crossBlack").eq(0).css("display", "none");
            $(".crossRed").eq(0).css("display", "block");
        } else {
            name_check[0].style.opacity = 0;
            wallet_name_tf[0].style.color = "#999999"
            wallet_name_tf[0].style.borderColor = "#999999";
            $(".crossBlack").eq(0).css("display", "none");
            $(".crossRed").eq(0).css("display", "none");
            $(".check").eq(0).css("display", "block");
        }

        activateNext("wallet_name","focus");
    }
    wallet_name_tf[0].onkeyup = function() {
        activateNext("wallet_name","keyUp");
    }
    $(".crossBlack").eq(0).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(0).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(0).click(function() {
        wallet_name_tf[0].value = "";
    });
    
    wallet_password_pw[0].onfocus = function() {
        wallet_password_pw[0].style.color = "#2b2b2b";
        wallet_password_pw[0].style.borderColor = "#36b25b";
        $(".check").eq(1).css("display", "none");
        $(".crossRed").eq(1).css("display", "none");
        $(".crossBlack").eq(1).css("display", "block");
    }
    wallet_password_pw[0].onfocusout = function() {
        if(crossBlackFlag === 1) {
            wallet_password_pw[0].value = "";
            crossBlackFlag = 0;
        }

        password = wallet_password_pw[0].value;
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if(password == null || password == "") {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            $(".check").eq(1).css("display", "none");
            $(".crossBlack").eq(1).css("display", "none");
            $(".crossRed").eq(1).css("display", "block");
        } else if(password.length < 8) {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            $(".check").eq(1).css("display", "none");
            $(".crossBlack").eq(1).css("display", "none");
            $(".crossRed").eq(1).css("display", "block");
        } else if(password_validate.length !== 3) {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; PW must contain a combination of letters, numbers, and special characters.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            $(".check").eq(1).css("display", "none");
            $(".crossBlack").eq(1).css("display", "none");
            $(".crossRed").eq(1).css("display", "block");
        } else {
            password_check[0].style.opacity = 0;
            wallet_password_pw[0].style.color = "#999999";
            wallet_password_pw[0].style.borderColor = "#999999";
            $(".crossBlack").eq(1).css("display", "none");
            $(".crossRed").eq(1).css("display", "none");
            $(".check").eq(1).css("display", "block");
        }

        activateNext("wallet_password","focus");

        if(!(password_confirm_pw[0].value === null 
            || password_confirm_pw[0].value === ""
            || password_confirm_pw[0].value.length == 0)) {
            $('.password_confirm').eq(0).focusout();
        }
    }
    wallet_password_pw[0].onkeyup = function() {
        activateNext("wallet_password","keyUp");
    }
    $(".crossBlack").eq(1).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(1).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(1).click(function() {
        wallet_password_pw[0].value = "";
    });

    password_confirm_pw[0].onfocus = function() {
        password_confirm_pw[0].style.color = "#2b2b2b";
        password_confirm_pw[0].style.borderColor = "#36b25b";
        $(".check").eq(2).css("display", "none");
        $(".crossRed").eq(2).css("display", "none");
        $('.crossBlack').eq(2).css("display", "block");
    }
    password_confirm_pw[0].onfocusout = function() {
        if(crossBlackFlag === 1) {
            password_confirm_pw[0].value = "";
            crossBlackFlag = 0;
        }

        password = wallet_password_pw[0].value;
        conf_password = password_confirm_pw[0].value;

        if(conf_password==""||conf_password==null) {
            conf_password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check[0].style.opacity = 1;
            password_confirm_pw[0].style.borderColor = "#910000";
            $('.check').eq(2).css("display", "none");
            $('.crossBlack').eq(2).css("display", "none");
            $('.crossRed').eq(2).css("display", "block");
        } else if(password !== conf_password) {
            conf_password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check[0].style.opacity = 1;
            password_confirm_pw[0].style.borderColor = "#910000";
            $('.check').eq(2).css("display", "none");
            $('.crossBlack').eq(2).css("display", "none");
            $('.crossRed').eq(2).css("display", "block");
        } else {
            conf_password_check[0].style.opacity = 0;
            password_confirm_pw[0].style.color = "#999999";
            password_confirm_pw[0].style.borderColor = "#999999";
            $('.crossBlack').eq(2).css("display", "none");
            $('.crossRed').eq(2).css("display", "none");
            $('.check').eq(2).css("display", "block");
        }

        activateNext("password_confirm", "focus");
    }
    password_confirm_pw[0].onkeyup = function() {
        activateNext("password_confirm", "keyUp");
    }
    $(".crossBlack").eq(2).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(2).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(2).click(function() {
        password_confirm_pw[0].value = "";
    });

    function activateNext(inputId, eventNm) {
        name = wallet_name_tf[0].value;
        password = wallet_password_pw[0].value;
        conf_password = password_confirm_pw[0].value;
        let validation_flag = 0;

        // Validate Name
        if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
        } else {
            validation_flag++;
        }

        // Validate Password
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if(password==""||password==null) {
        } else if(password.length<8) {
        } else if(password_validate.length !== 3) {
        } else {
            validation_flag++;
        }

        // Validate Password confirm
        if(conf_password==""||conf_password==null) {
        } else if(password !== conf_password) {
        } else {
            validation_flag++;
        }

        //  Activate or Deactivate Next Button
        if(validation_flag == 3) {
            $('.check').css("display", "block");
            $('.crossBlack').css("display", "none");
            $('.crossRed').css("display", "none");
            wallet_name_tf[0].style.color = "#999999";
            wallet_password_pw[0].style.color = "#999999";
            password_confirm_pw[0].style.color = "#999999";
            wallet_name_tf[0].style.borderColor = "#999999";
            wallet_password_pw[0].style.borderColor = "#999999";
            password_confirm_pw[0].style.borderColor = "#999999";
            name_check[0].style.opacity = 0;
            password_check[0].style.opacity = 0;
            conf_password_check[0].style.opacity = 0;
            $('.next').attr("src","img/new/btn_next.png");
            $('.phase-next').css("cursor","pointer");

            // For refocus
            if(eventNm === "keyUp") {
                if(inputId == "wallet_name") {
                    $('.wallet_name').eq(0).focus();
                    inputId =="";
                } else if(inputId == "wallet_password") {
                    $('.wallet_password').eq(0).focus();
                    
                } else if(inputId == "password_confirm") {
                    $('.password_confirm').eq(0).focus();
                }
            }
        } else {
            $('.next').attr("src","img/new/btn_next_none.png");
            $('.phase-next').css("cursor","default");
        }
    }

    phase_back[0].onclick = function() {
        backCreatePhase2();
        create_modal_content[0].style.display = "none";
        $('.progressPhases').css("display","none");
    }

    phase_next[0].onclick = function() {
        name = wallet_name_tf[0].value;
        password = wallet_password_pw[0].value;
        conf_password = password_confirm_pw[0].value;
        let validation_flag = 0;

        // Validate Name
        if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.';
            name_check[0].style.opacity = 1;
            wallet_name_tf[0].style.borderColor = "#910000";
            wallet_name_tf[0].value = "";
        } else {
            name_check[0].style.opacity = 0;
            wallet_name_tf[0].style.borderColor = "#000000";
            validation_flag++;
        }

        // Validate Password
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if(password==""||password==null) {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            wallet_password_pw[0].value = "";
        } else if(password.length<8) {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            wallet_password_pw[0].value = "";
        } else if(password_validate.length !== 3) {
            password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain a combination of letters, numbers, and special characters.';
            password_check[0].style.opacity = 1;
            wallet_password_pw[0].style.borderColor = "#910000";
            wallet_password_pw[0].value = "";
        } else {
            password_check[0].style.opacity = 0;
            wallet_password_pw[0].style.borderColor = "#000000";
            validation_flag++;
        }

        // Validate Password confirm
        if(conf_password==""||conf_password==null) {
            conf_password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check[0].style.opacity = 1;
            password_confirm_pw[0].style.borderColor = "#910000";
            password_confirm_pw[0].value = "";
        } else if(password !== conf_password) {
            conf_password_check[0].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check[0].style.opacity = 1;
            password_confirm_pw[0].style.borderColor = "#910000";
            password_confirm_pw[0].value = "";
        } else {
            conf_password_check[0].style.opacity = 0;
            password_confirm_pw[0].style.borderColor = "#000000";
            validation_flag++;
        }

        // Move next page
        if(validation_flag == 3) {
            create_modal_content[0].style.display = "none";
            create_modal_content[1].style.display = "block";
            createPhase3();
            app.createKeystore(null, wallet_name_tf[0].value, wallet_password_pw[0].value);
        }
    }

    $(".pwCover").eq(0).click(function() {
        $(".wallet_password").eq(0).attr("type", "text");
        $(".pwCover").eq(0).css("display", "none");
        $(".pwUncover").eq(0).css("display", "block");
    });
    $(".confirmPwCover").eq(0).click(function() {
        $(".password_confirm").eq(0).attr("type", "text");
        $(".confirmPwCover").eq(0).css("display", "none");
        $(".confirmPwUncover").eq(0).css("display", "block");
    });
    $(".pwUncover").eq(0).click(function() {
        $(".wallet_password").eq(0).attr("type", "password");
        $(".pwCover").eq(0).css("display", "block");
        $(".pwUncover").eq(0).css("display", "none");
    });
    $(".confirmPwUncover").eq(0).click(function() {
        $(".password_confirm").eq(0).attr("type", "password");
        $(".confirmPwCover").eq(0).css("display", "block");
        $(".confirmPwUncover").eq(0).css("display", "none");
    });

    download_keystore[0].onclick = function() {
        download_keystore_flag = 1;
        $('.next').attr("src","img/new/btn_next.png");
        $('.phase-next').css("cursor","pointer");
        app.downloadKeystore();
        option_modal[0].style.display = "block";
        modal_option_content[1].style.display = "block";
    }

    phase_back[1].onclick = function() {
        create_modal_content[1].style.display = "none";
        create_modal_content[0].style.display = "block";
        backCreatePhase3();
        app.deleteKeystore();
        app.resetKeystore();
        download_keystore_flag = 0;
    }

    phase_next[1].onclick = function() {
        if(download_keystore_flag == 0) {
            option_modal[0].style.display = "block";
            modal_option_content[0].style.display = "block";
        } else {
            let private_key_cover = '';

            for(let i=0; i<app.getPrivateKey().length; i++) {
                private_key_cover += '*';
            }

            private_key.value = private_key_cover;
            
            createPhase4();
            create_modal_content[1].style.display = "none";
            create_modal_content[2].style.display = "block";
        }
    }

    $('#no').click(function() {
        option_modal[0].style.display = "none";
        modal_option_content[0].style.display = "none";
    });

    $('#yes').click(function() {
        let private_key_cover = '';

        for(let i=0; i<app.getPrivateKey().length; i++) {
            private_key_cover += '*';
        }

        private_key.value = private_key_cover;

        createPhase4();
        option_modal[0].style.display = "none";
        modal_option_content[0].style.display = "none";
        create_modal_content[1].style.display = "none";
        create_modal_content[2].style.display = "block";
    });

    $('#confirm').click(function() {
        option_modal[0].style.display = "none";
        modal_option_content[1].style.display = "none";
    });

    private_key_uncover_img.onclick = function() {
        let private_key_cover = '';

        for(let i=0; i<app.getPrivateKey().length; i++) {
            private_key_cover += '*';
        }

        private_key.value = private_key_cover;

        private_key.style.color = "#999999";
        private_key.style.borderColor = "#999999";
        private_key_uncover_img.style.display = "none";
        private_key_cover_img.style.display = "block";
    }

    private_key_cover_img.onclick = function() {
        private_key.value = app.getPrivateKey();
        private_key.style.color = "#000000";
        private_key.style.borderColor = "#000000";
        private_key_cover_img.style.display = "none";
        private_key_uncover_img.style.display = "block";
    }

    $('#copy').click(function() {
        // copy_complete[0].style.display = "block";
        let private_key_copy = document.createElement("INPUT");
        private_key_copy.setAttribute("type", "text");
        private_key_copy.setAttribute("value", app.getPrivateKey());
        document.body.appendChild(private_key_copy);
        private_key_copy.select();
        document.execCommand('copy');
        document.body.removeChild(private_key_copy);
        // Copy Complete Timer
        // setTimeout(function() {copy_complete[0].style.display = "none"}, 2000);
    });

    phase_next[2].onclick = function() {
        create_modal_content[2].style.display = "none";
        $('.progressPhases').css("display","none");
        app.createWalletComplete();
        locationHref("main", main_start);
    }

    phase_back[2].onclick = function() {
        backCreatePhase4();
        create_modal_content[2].style.display = "none";
        create_modal_content[1].style.display = "block";
    }


    // Load wallet function
    load_wallet_btn.onclick = function() {
        loadPhase2();
        keystore_file_name[0].style.display = "none";
        drag_and_drop[0].style.borderColor = "#C4C5C6";
        file_form_check.style.opacity = 0;
        load_password_pw.value = '';
        load_password_check.style.opacity = 0;
        load_password_pw.style.borderColor = "#000000";
        $('#loadWalletRadio1').prop('checked', true);
        $('.radioCheck p:nth-child(1) img').attr("src", "img/new/btn_check_red.png");
        $('.radioCheck p:nth-child(2) img').attr("src", "img/new/btn_check_grey.png");
        $('.check').css("display", "none");
        $('.crossBlack').css("display", "none");
        $('.crossRed').css("display", "none");
        $('.progressPhases').css("display", "block");
        load_modal_content[0].style.display = "block";
    }

    $('.introGroupDetails:nth-child(2) div:nth-child(3)').click(function() {
        load_wallet_btn.click();
    });

    $('.load-phase-2-middle label').eq(0).click(function() {
        $('.radioCheck p:nth-child(1) img').attr("src", "img/new/btn_check_red.png");
        $('.radioCheck p:nth-child(2) img').attr("src", "img/new/btn_check_grey.png");
        $(".introGroupIndex p:nth-child(5)").css("opacity", "0");
        $("#navSpan img:nth-child(4)").css("display", "none");
    });
    $('.load-phase-2-middle label').eq(1).click(function() {
        $('.radioCheck p:nth-child(2) img').attr("src", "img/new/btn_check_red.png");
        $('.radioCheck p:nth-child(1) img').attr("src", "img/new/btn_check_grey.png");
        $(".introGroupIndex p:nth-child(5)").css("opacity", "0.5");
        $("#navSpan img:nth-child(4)").css("display", "inline");
    });
    $('.radioCheck p:nth-child(1) img').click(function() {
        $('.load-phase-2-middle label').eq(0).click();
    });
    $('.radioCheck p:nth-child(2) img').click(function() {
        $('.load-phase-2-middle label').eq(1).click();
    });

    phase_back[3].onclick = function() {
        backLoadPhase2();
        load_modal_content[0].style.display = "none";
        $('.progressPhases').css("display","none");
    }

    phase_next[3].onclick = function() {
        if(load_radio[0].checked) {
            loadPhase3();
            load_modal_content[0].style.display = "none";
            load_modal_content[1].style.display = "block";
        } else {
            loadPhase4();
            load_modal_content[0].style.display = "none";
            load_modal_content[2].style.display = "block";
        }
    }

    phase_back[4].onclick = function() {
        backLoadPhase3();
        load_modal_content[1].style.display = "none";
        load_modal_content[0].style.display = "block";
    }

    load_password_pw.onkeyup = function() {
        load_password = load_password_pw.value;

        if(load_password == "" || load_password == null) {
            load_password_check.innerHTML = 'Please enter your password.';
            load_password_check.style.opacity = 1;
            $('.next').attr("src", "img/new/btn_load_grey.png");
            $('.phase-next').css("cursor", "default");
        } else {
            load_password_check.style.opacity = 0;
            $('.next').attr("src", "img/new/btn_load_red.png");
            $('.phase-next').css("cursor", "pointer");
        }
    }
    load_password_pw.onfocus = function() {
        load_password_pw.style.color = "#2b2b2b";
        load_password_pw.style.borderColor = "#36b25b";
        $(".check").eq(3).css("display", "none");
        $(".crossRed").eq(3).css("display", "none");
        $(".crossBlack").eq(3).css("display", "block");
    }
    load_password_pw.onfocusout = function() {
        if(crossBlackFlag === 1) {
            load_password_pw.value = "";
            crossBlackFlag = 0;
            $('.next').attr("src", "img/new/btn_load_grey.png");
            $('.phase-next').css("cursor", "default");
        }

        load_password = load_password_pw.value;

        if(load_password == "" || load_password == null) {
            load_password_check.innerHTML = 'Please enter your password.';
            load_password_check.style.opacity = 1;
            load_password_pw.style.borderColor = "#910000";
            $('.check').eq(3).css("display", "none");
            $(".crossBlack").eq(3).css("display", "none");
            $(".crossRed").eq(3).css("display", "block");
        } else {
            load_password_pw.style.color = "#999999"
            load_password_pw.style.borderColor = "#999999";
            $(".crossBlack").eq(3).css("display", "none");
            $(".crossRed").eq(3).css("display", "none");
            $('.check').eq(3).css("display", "block");
        }
    }
    $(".crossBlack").eq(3).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(3).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(3).click(function() {
        load_password_pw.value = "";
        $('.next').attr("src", "img/new/btn_load_grey.png");
        $('.phase-next').css("cursor", "default");
        load_password_check.innerHTML = 'Please enter your password.';
    });

    phase_next[4].onclick = function() {
        load_password = load_password_pw.value;

        if(load_password == "" || load_password == null) {
            load_password_check.innerHTML = 'Please enter your password.';
            load_password_check.style.opacity = 1;
            load_password_pw.style.borderColor = "#910000";
            $('.check').eq(3).css("display", "none");
            $(".crossBlack").eq(3).css("display", "none");
            $(".crossRed").eq(3).css("display", "block");
        } else if((!app.matchPassword(load_password)) || (fileValidationFlag != "CorrectFileForm")) {
            load_password_check.innerHTML = 'Please check your keystore file and match the password.';
            load_password_check.style.opacity = 1;
            load_password_pw.style.borderColor = "#910000";
            $('.check').eq(3).css("display", "none");
            $(".crossBlack").eq(3).css("display", "none");
            $(".crossRed").eq(3).css("display", "block");
        } else {
            load_modal_content[1].style.display = "none";
            $('.progressPhases').css("display","none");
            locationHref("main", main_start);
        }
    }

    select_keystore[0].onclick = function() {
        fileValidationFlag = app.openFileReader();

        if(fileValidationFlag == "CorrectFileForm") {
            keystore_file_name_font.innerHTML = app.getFileName();
            keystore_file_name[0].style.display = "block";
            keystore_file_name[0].style.background = "#999999";
            file_form_check.style.opacity = 0;
        } else if(fileValidationFlag == "IncorrectFileForm") {
            keystore_file_name_font.innerHTML = app.getFileName();
            keystore_file_name[0].style.display = "block";
            keystore_file_name[0].style.background = "#910000";
            file_form_check.style.opacity = 1;
        } else if(fileValidationFlag == "FileException") {
            keystore_file_name[0].style.display = "none";
            file_form_check.style.opacity = 0;
        } else {
            app.errorPopup();
        }
    }

    close_file[0].onclick = function() {
        fileValidationFlag == "FileException";
        keystore_file_name[0].style.display = "none";
        file_form_check.style.opacity = 0;
    }

    $("#loadPwCover").click(function() {
        $("#load_wallet_password").attr("type", "text");
        $("#loadPwCover").css("display", "none");
        $("#loadPwUncover").css("display", "block");
    });
    $("#loadPwUncover").click(function() {
        $("#load_wallet_password").attr("type", "password");
        $("#loadPwUncover").css("display", "none");
        $("#loadPwCover").css("display", "block");
    });

    phase_back[5].onclick = function() {
        backLoadPhase4();
        load_modal_content[2].style.display = "none";
        load_modal_content[0].style.display = "block";
    }

    $('#load_wallet_privateKey').keyup(function() {
        load_privateKey = $('#load_wallet_privateKey').val();

        if(load_privateKey == "" || load_privateKey == null) {
            $('#load_privateKey_check').html("Please enter your private key.");
            $('#load_privateKey_check').css('opacity', '1');
            $('.next').attr("src", "img/new/btn_next_none.png");
            $('.phase-next').css("cursor", "default");
        } else {
            $('#load_privateKey_check').css('opacity', '0');
            $('.next').attr("src", "img/new/btn_next.png");
            $('.phase-next').css("cursor", "pointer");
        }
    });
    $('#load_wallet_privateKey').focusin(function() {
        $('#load_wallet_privateKey').css('borderColor', '#36b25b');
        $('#load_wallet_privateKey').css('color', '#2b2b2b');
        $(".check").eq(4).css("display", "none");
        $(".crossRed").eq(4).css("display", "none");
        $(".crossBlack").eq(4).css("display", "block");
    });
    $('#load_wallet_privateKey').focusout (function() {
        if(crossBlackFlag === 1) {
            $('#load_wallet_privateKey').val('');
            crossBlackFlag = 0;
            $('.next').attr("src", "img/new/btn_next_none.png");
            $('.phase-next').css("cursor", "default");
        }

        load_privateKey = $('#load_wallet_privateKey').val();

        if(load_privateKey == "" || load_privateKey == null) {
            $('#load_privateKey_check').html("Please enter your private key.");
            $('#load_privateKey_check').css('opacity', '1');
            $('#load_wallet_privateKey').css('borderColor', '#910000');
            $('.check').eq(4).css("display", "none");
            $(".crossBlack").eq(4).css("display", "none");
            $(".crossRed").eq(4).css("display", "block");
        } else {
            $('#load_wallet_privateKey').css('color', '#999999');
            $('#load_wallet_privateKey').css('borderColor', '#999999');
            $(".crossBlack").eq(4).css("display", "none");
            $(".crossRed").eq(4).css("display", "none");
            $('.check').eq(4).css("display", "block");
        }
    });
    $(".crossBlack").eq(4).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(4).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(4).click(function() {
        $('#load_wallet_privateKey').val('');
        $('.next').attr("src", "img/new/btn_next_none.png");
        $('.phase-next').css("cursor", "default");
        $('#load_privateKey_check').html("Please enter your private key.");
    });

    phase_next[5].onclick = function() {
        load_privateKey = $('#load_wallet_privateKey').val();

        if(load_privateKey == "" || load_privateKey == null) {
            $('#load_privateKey_check').text('Please enter your private key.');
            $('#load_privateKey_check').css('opacity', '1');
            $('#load_wallet_privateKey').css('borderColor', '#910000');
            $('.check').eq(4).css("display", "none");
            $(".crossBlack").eq(4).css("display", "none");
            $(".crossRed").eq(4).css("display", "block");
        } else if(load_privateKey.match(privateKey_validate) || (load_privateKey.length !== 64)) {
            $('#load_privateKey_check').text('Incorrect private key.');
            $('#load_privateKey_check').css('opacity', '1');
            $('#load_wallet_privateKey').css('borderColor', '#910000');
            $('.check').eq(4).css("display", "none");
            $(".crossBlack").eq(4).css("display", "none");
            $(".crossRed").eq(4).css("display", "block");
        } else {
            loadPhase5();
            // Reset values
            wallet_name_tf[1].value = '';
            wallet_password_pw[1].value = '';
            password_confirm_pw[1].value = '';
            // Init margin Setting
            phase_next[6].click();
            name_check[1].style.opacity = 0;
            password_check[1].style.opacity = 0;
            conf_password_check[1].style.opacity = 0;
            wallet_name_tf[1].style.borderColor = "#000000";
            wallet_password_pw[1].style.borderColor = "#000000";
            password_confirm_pw[1].style.borderColor = "#000000";
            load_modal_content[2].style.display = "none";
            load_modal_content[3].style.display = "block";
            $('.check').css("display", "none");
            $('.crossBlack').css("display", "none");
            $('.crossRed').css("display", "none");
            load_modal_content[2].style.display = "none";
            load_modal_content[3].style.display = "block";
        }
    }

    $("#loadPkCover").click(function() {
        $("#load_wallet_privateKey").attr("type", "text");
        $("#loadPkCover").css("display", "none");
        $("#loadPkUncover").css("display", "block");
    });
    $("#loadPkUncover").click(function() {
        $("#load_wallet_privateKey").attr("type", "password");
        $("#loadPkUncover").css("display", "none");
        $("#loadPkCover").css("display", "block");
    });

    
    phase_back[6].onclick = function() {
        backLoadPhase5();
        load_modal_content[3].style.display = "none";
        load_modal_content[2].style.display = "block";
    }

    wallet_name_tf[1].onfocus = function() {
        wallet_name_tf[1].style.color = "#2b2b2b";
        wallet_name_tf[1].style.borderColor = "#36b25b";
        $(".check").eq(5).css("display", "none");
        $(".crossRed").eq(5).css("display", "none");
        $(".crossBlack").eq(5).css("display", "block");
    }
    wallet_name_tf[1].onfocusout = function() {
        if(crossBlackFlag === 1) {
            wallet_name_tf[1].value = "";
            crossBlackFlag = 0;
        }

        name = wallet_name_tf[1].value;

        if(name == null || name == "" || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.';
            name_check[1].style.opacity = 1;
            wallet_name_tf[1].style.borderColor = "#910000";
            $('.check').eq(5).css("display", "none");
            $(".crossBlack").eq(5).css("display", "none");
            $(".crossRed").eq(5).css("display", "block");
        } else {
            name_check[1].style.opacity = 0;
            wallet_name_tf[1].style.color = "#999999"
            wallet_name_tf[1].style.borderColor = "#999999";
            $(".crossBlack").eq(5).css("display", "none");
            $(".crossRed").eq(5).css("display", "none");
            $(".check").eq(5).css("display", "block");
        }

        activateLoad("wallet_name","focus");
    }
    wallet_name_tf[1].onkeyup = function() {
        activateLoad("wallet_name","keyUp");
    }
    $(".crossBlack").eq(5).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(5).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(5).click(function() {
        wallet_name_tf[1].value = "";
    });
    
    wallet_password_pw[1].onfocus = function() {
        wallet_password_pw[1].style.color = "#2b2b2b";
        wallet_password_pw[1].style.borderColor = "#36b25b";
        $(".check").eq(6).css("display", "none");
        $(".crossRed").eq(6).css("display", "none");
        $(".crossBlack").eq(6).css("display", "block");
    }
    wallet_password_pw[1].onfocusout = function() {
        if(crossBlackFlag === 1) {
            wallet_password_pw[1].value = "";
            crossBlackFlag = 0;
        }

        password = wallet_password_pw[1].value;
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if(password == null || password == "") {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            $(".check").eq(6).css("display", "none");
            $(".crossBlack").eq(6).css("display", "none");
            $(".crossRed").eq(6).css("display", "block");
        } else if(password.length < 8) {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            $(".check").eq(6).css("display", "none");
            $(".crossBlack").eq(6).css("display", "none");
            $(".crossRed").eq(6).css("display", "block");
        } else if(password_validate.length !== 3) {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; PW must contain a combination of letters, numbers, and special characters.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            $(".check").eq(6).css("display", "none");
            $(".crossBlack").eq(6).css("display", "none");
            $(".crossRed").eq(6).css("display", "block");
        } else {
            password_check[1].style.opacity = 0;
            wallet_password_pw[1].style.color = "#999999";
            wallet_password_pw[1].style.borderColor = "#999999";
            $(".crossBlack").eq(6).css("display", "none");
            $(".crossRed").eq(6).css("display", "none");
            $(".check").eq(6).css("display", "block");
        }

        activateLoad("wallet_password","focus");

        if(!(password_confirm_pw[1].value === null 
            || password_confirm_pw[1].value === ""
            || password_confirm_pw[1].value.length == 0)) {
            $('.password_confirm').eq(1).focusout();
        }
    }
    wallet_password_pw[1].onkeyup = function() {
        activateLoad("wallet_password","keyUp");
    }
    $(".crossBlack").eq(6).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(6).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(6).click(function() {
        wallet_password_pw[1].value = "";
    });

    password_confirm_pw[1].onfocus = function() {
        password_confirm_pw[1].style.color = "#2b2b2b";
        password_confirm_pw[1].style.borderColor = "#36b25b";
        $(".check").eq(7).css("display", "none");
        $(".crossRed").eq(7).css("display", "none");
        $('.crossBlack').eq(7).css("display", "block");
    }
    password_confirm_pw[1].onfocusout = function() {
        if(crossBlackFlag === 1) {
            password_confirm_pw[1].value = "";
            crossBlackFlag = 0;
        }

        password = wallet_password_pw[1].value;
        conf_password = password_confirm_pw[1].value;

        if(conf_password == "" || conf_password == null) {
            conf_password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check[1].style.opacity = 1;
            password_confirm_pw[1].style.borderColor = "#910000";
            $('.check').eq(7).css("display", "none");
            $('.crossBlack').eq(7).css("display", "none");
            $('.crossRed').eq(7).css("display", "block");
        } else if(password !== conf_password) {
            conf_password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check[1].style.opacity = 1;
            password_confirm_pw[1].style.borderColor = "#910000";
            $('.check').eq(7).css("display", "none");
            $('.crossBlack').eq(7).css("display", "none");
            $('.crossRed').eq(7).css("display", "block");
        } else {
            conf_password_check[1].style.opacity = 0;
            password_confirm_pw[1].style.color = "#999999";
            password_confirm_pw[1].style.borderColor = "#999999";
            $('.crossBlack').eq(7).css("display", "none");
            $('.crossRed').eq(7).css("display", "none");
            $('.check').eq(7).css("display", "block");
        }

        activateLoad("password_confirm", "focus");
    }
    password_confirm_pw[1].onkeyup = function() {
        activateLoad("password_confirm", "keyUp");
    }
    $(".crossBlack").eq(7).mouseenter(function() {
        crossBlackFlag = 1;
    });
    $(".crossBlack").eq(7).mouseleave(function() {
        crossBlackFlag = 0;
    });
    $(".crossRed").eq(7).click(function() {
        password_confirm_pw[1].value = "";
    });

    function activateLoad(inputId, eventNm) {
        name = wallet_name_tf[1].value;
        password = wallet_password_pw[1].value;
        conf_password = password_confirm_pw[1].value;
        let validation_flag = 0;

        // Validate Name
        if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
        } else {
            validation_flag++;
        }

        // Validate Password
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if( password == null || password == "") {
        } else if(password.length<8) {
        } else if(password_validate.length !== 3) {
        } else {
            validation_flag++;
        }

        // Validate Password confirm
        if(conf_password == null || conf_password == "") {
        } else if(password !== conf_password) {
        } else {
            validation_flag++;
        }

        //  Activate or Deactivate Next Button
        if(validation_flag == 3) {
            $('.check').css("display", "block");
            $('.crossBlack').css("display", "none");
            $('.crossRed').css("display", "none");
            wallet_name_tf[1].style.color = "#999999";
            wallet_password_pw[1].style.color = "#999999";
            password_confirm_pw[1].style.color = "#999999";
            wallet_name_tf[1].style.borderColor = "#999999";
            wallet_password_pw[1].style.borderColor = "#999999";
            password_confirm_pw[1].style.borderColor = "#999999";
            name_check[1].style.opacity = 0;
            password_check[1].style.opacity = 0;
            conf_password_check[1].style.opacity = 0;
            $('.next').attr("src","img/new/btn_load_red.png");
            $('.phase-next').css("cursor","pointer");

            // For refocus
            if(eventNm === "keyUp") {
                if(inputId == "wallet_name") {
                    $('.wallet_name').eq(1).focus();
                    inputId =="";
                } else if(inputId == "wallet_password") {
                    $('.wallet_password').eq(1).focus();
                    
                } else if(inputId == "password_confirm") {
                    $('.password_confirm').eq(1).focus();
                }
            }
        } else {
            $('.next').attr("src","img/new/btn_load_grey.png");
            $('.phase-next').css("cursor","default");
        }
    }

    phase_next[6].onclick = function() {

        name = wallet_name_tf[1].value;
        password = wallet_password_pw[1].value;
        conf_password = password_confirm_pw[1].value;
        let validation_flag = 0;

        // Validate Name
        if(name == null || name == "" || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.';
            name_check[1].style.opacity = 1;
            wallet_name_tf[1].style.borderColor = "#910000";
            wallet_name_tf[1].value = "";
        } else {
            name_check[1].style.opacity = 0;
            wallet_name_tf[1].style.borderColor = "#000000";
            validation_flag++;
        }

        // Validate Password
        password_validate = [];

        if(password.match(letters)) {
            password_validate.push(true);
        }
        if(password.match(numbers)) {
            password_validate.push(true);
        }
        if(password.match(specials)) {
            password_validate.push(true);
        }

        if(password == null || password == "") {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            wallet_password_pw[1].value = "";
        } else if(password.length < 8) {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            wallet_password_pw[1].value = "";
        } else if(password_validate.length !== 3) {
            password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain a combination of letters, numbers, and special characters.';
            password_check[1].style.opacity = 1;
            wallet_password_pw[1].style.borderColor = "#910000";
            wallet_password_pw[1].value = "";
        } else {
            password_check[1].style.opacity = 0;
            wallet_password_pw[1].style.borderColor = "#000000";
            validation_flag++;
        }

        // Validate Password confirm
        if(conf_password == null || conf_password == "") {
            conf_password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check[1].style.opacity = 1;
            password_confirm_pw[1].style.borderColor = "#910000";
            password_confirm_pw[1].value = "";
        } else if(password !== conf_password) {
            conf_password_check[1].innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check[1].style.opacity = 1;
            password_confirm_pw[1].style.borderColor = "#910000";
            password_confirm_pw[1].value = "";
        } else {
            conf_password_check[1].style.opacity = 0;
            password_confirm_pw[1].style.borderColor = "#000000";
            validation_flag++;
        }

        // Move next page
        if(validation_flag == 3) {
            app.createKeystore(null, wallet_name_tf[1].value, wallet_password_pw[1].value);
            load_modal_content[4].style.display = "none";
            $('.progressPhases').css("display","none");            
            locationHref("main", main_start);
        }
    }

    $(".pwCover").eq(1).click(function() {
        $(".wallet_password").eq(1).attr("type", "text");
        $(".pwCover").eq(1).css("display", "none");
        $(".pwUncover").eq(1).css("display", "block");
    });
    $(".confirmPwCover").eq(1).click(function() {
        $(".password_confirm").eq(1).attr("type", "text");
        $(".confirmPwCover").eq(1).css("display", "none");
        $(".confirmPwUncover").eq(1).css("display", "block");
    });
    $(".pwUncover").eq(1).click(function() {
        $(".wallet_password").eq(1).attr("type", "password");
        $(".pwCover").eq(1).css("display", "block");
        $(".pwUncover").eq(1).css("display", "none");
    });
    $(".confirmPwUncover").eq(1).click(function() {
        $(".password_confirm").eq(1).attr("type", "password");
        $(".confirmPwCover").eq(1).css("display", "block");
        $(".confirmPwUncover").eq(1).css("display", "none");
    });



    // **modal off when window click
        // window.onclick = function(event) {
        //     if(event.target == modal) {
        //         modal.style.display = "none";
        //     }
        // }




    uiInitStatus();
    setHiddenHeaderAndFooter(true);
      console.log("intro_start");
  }