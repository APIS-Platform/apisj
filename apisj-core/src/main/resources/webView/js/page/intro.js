
  function intro_start(){

    // Create wallet modal
    let create_modal = document.getElementById('create-wallet-modal');
    let create_wallet_btn = document.getElementById('create-wallet-btn');
    let close = document.querySelectorAll('.close');
    let phase_next = document.querySelectorAll('.phase-next');
    let phase_back = document.querySelectorAll('.phase-back');
    let create_modal_content = document.querySelectorAll('.create-modal-content');
    let wallet_name_tf = document.getElementById("wallet_name");
    let wallet_password_pw = document.getElementById("wallet_password");
    let password_confirm_pw = document.getElementById("password_confirm");
    let name_check = document.getElementById("name_check");
    let password_check = document.getElementById("password_check");
    let conf_password_check = document.getElementById("conf_password_check");
    // For validating values
    let name = wallet_name_tf.value;
    let password = wallet_password_pw.value;
    let conf_password = password_confirm_pw.value;
    let password_validate = [];
    let letters = /[a-zA-Z]/g;
    let numbers = /[0-9]/g;
    let specials = /[^(a-zA-Z0-9)]/g;
    let load_wallet_btn = document.getElementById('load-wallet-btn');
    
    // Create & Load Wallet mouseover
    create_wallet_btn.onmouseover = function() {
        create_wallet_btn.style.background = "url('img/new/btn_create_wallet.png') no-repeat center";
        load_wallet_btn.style.background = "url('img/new/btn_load_wallet_none.png') no-repeat center";
        $(".introGroupDetails:nth-child(2) div:nth-child(2) font").css("opacity","1");
        $(".introGroupDetails:nth-child(2) div:nth-child(3) font").css("opacity","0.3");
        $("#plusWhite").attr("src","img/new/icon_plus_white.png");
        $("#downArrowWhite").attr("src","img/new/icon_down_arrow_white_none.png");
    }
    load_wallet_btn.onmouseover = function() {
        create_wallet_btn.style.background = "url('img/new/btn_create_wallet_none.png') no-repeat center";
        load_wallet_btn.style.background = "url('img/new/btn_load_wallet.png') no-repeat center";
        $(".introGroupDetails:nth-child(2) div:nth-child(2) font").css("opacity","0.3");
        $(".introGroupDetails:nth-child(2) div:nth-child(3) font").css("opacity","1");
        $("#plusWhite").attr("src","img/new/icon_plus_white_none.png");
        $("#downArrowWhite").attr("src","img/new/icon_down_arrow_white.png");
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
        wallet_name_tf.value = '';
        wallet_password_pw.value = '';
        password_confirm_pw.value = '';
        name_check.style.opacity = 0;
        password_check.style.opacity = 0;
        conf_password_check.style.opacity = 0;
        wallet_name_tf.style.borderColor = "#000000";
        wallet_password_pw.style.borderColor = "#000000";
        password_confirm_pw.style.borderColor = "#000000";
        $('.progressPhases').css("display","block");
        create_modal_content[0].style.display = "block";
        download_keystore_flag = 0;
        app.resetKeystore();
    }

    $('.introGroupDetails:nth-child(2) div:nth-child(2)').click(function() {
        create_wallet_btn.click();
    });

    // Intro Group Change
    function phase1() {
        $(".introGroupIndex p:nth-child(1)").css("opacity","1");
        $(".introGroupIndex p:nth-child(2)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(2)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupIndex p:nth-child(3)").text('02');
        $(".introGroupDetails:nth-child(3)").css("display","none");
        $(".introGroupDetails:nth-child(2)").css("display","block");
        $("#navSpan img:nth-child(2)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(1)").attr("src","img/new/icon_nav.png");
    }

    function createPhase2() {
        $(".introGroupIndex p:nth-child(1)").css("opacity","0.5");
        $(".introGroupIndex p:nth-child(2)").css("opacity","1");
        $(".introGroupIndex p:nth-child(2)").text("02");
        $(".introGroupIndex p:nth-child(3)").html('<img src="img/new/icon_vertical_line.png" alt="vertical_line" id="verticalLine">');
        $(".introGroupDetails:nth-child(2)").css("display","none");
        $(".introGroupDetails:nth-child(3)").css("display","block");
        $("#navSpan img:nth-child(1)").attr("src","img/new/icon_nav_circle.png");
        $("#navSpan img:nth-child(2)").attr("src","img/new/icon_nav.png");

        uiInitStatus();
    }

    // close[0].onclick = function() {
    //     create_modal_content[0].style.display = "none";
    //     $('.progressPhases').css("display","none");
    // }

    // close[1].onclick = function() {
    //     app.deleteKeystore();
    //     create_modal_content[1].style.display = "none";
    //     create_modal.style.display = "none";
    // }

    // close[2].onclick = function() {
    //     app.deleteKeystore();
    //     create_modal_content[2].style.display = "none";
    //     create_modal.style.display = "none";
    // }

    wallet_name_tf.onfocus = function() {
        wallet_name_tf.style.borderColor = "#36b25b";
    }
    wallet_name_tf.onfocusout = function() {
        name = wallet_name_tf.value;

        if(name == null || name == "" || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.(*no space with first and end letters)';
            name_check.style.opacity = 1;
            wallet_name_tf.style.borderColor = "#910000";
        } else {
            name_check.style.opacity = 0;
            wallet_name_tf.style.borderColor = "#000000";
        }

        activateNext();
    }

    wallet_password_pw.onfocus = function() {
        wallet_password_pw.style.borderColor = "#36b25b";
    }
    wallet_password_pw.onfocusout = function() {
        password = wallet_password_pw.value;
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
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
        } else if(password.length<8) {
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
        } else if(password_validate.length !== 3) {
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; PW must contain a combination of letters, numbers, and special characters.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
        } else {
            password_check.style.opacity = 0;
            wallet_password_pw.style.borderColor = "#000000";
        }

        activateNext();
    }

    password_confirm_pw.onfocus = function() {
        password_confirm_pw.style.borderColor = "#36b25b";
    }
    password_confirm_pw.onfocusout = function() {
        password = wallet_password_pw.value;
        conf_password = password_confirm_pw.value;

        if(conf_password==""||conf_password==null) {
            conf_password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check.style.opacity = 1;
            password_confirm_pw.style.borderColor = "#910000";
        } else if(password !== conf_password) {
            conf_password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check.style.opacity = 1;
            password_confirm_pw.style.borderColor = "#910000";
        } else {
            conf_password_check.style.opacity = 0;
            password_confirm_pw.style.borderColor = "#000000";
        }

        activateNext();
    }
    password_confirm_pw.onkeyup = function() {
        activateNext();
    }

    function activateNext() {
        name = wallet_name_tf.value;
        password = wallet_password_pw.value;
        conf_password = password_confirm_pw.value;
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
            wallet_name_tf.style.borderColor = "#000000";
            wallet_password_pw.style.borderColor = "#000000";
            password_confirm_pw.style.borderColor = "#000000";
            name_check.style.opacity = 0;
            password_check.style.opacity = 0;
            conf_password_check.style.opacity = 0;
            $('#next').attr("src","img/new/btn_next.png");
            $('.phase-next').css("cursor","pointer");
        } else {
            $('#next').attr("src","img/new/btn_next_none.png");
            $('.phase-next').css("cursor","default");
        }
    }

    phase_back[0].onclick = function() {
        phase1();
        create_modal_content[0].style.display = "none";
        $('.progressPhases').css("display","none");
    }

    phase_next[0].onclick = function() {
        name = wallet_name_tf.value;
        password = wallet_password_pw.value;
        conf_password = password_confirm_pw.value;
        let validation_flag = 0;

        // Validate Name
        if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
            name_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Enter new wallet name.';
            name_check.style.opacity = 1;
            wallet_name_tf.style.borderColor = "#910000";
            wallet_name_tf.value = "";
        } else {
            name_check.style.opacity = 0;
            wallet_name_tf.style.borderColor = "#000000";
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
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Please Enter your password.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
            wallet_password_pw.value = "";
        } else if(password.length<8) {
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain at least 8 characters.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
            wallet_password_pw.value = "";
        } else if(password_validate.length !== 3) {
            password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Password must contain a combination of letters, numbers, and special characters.';
            password_check.style.opacity = 1;
            wallet_password_pw.style.borderColor = "#910000";
            wallet_password_pw.value = "";
        } else {
            password_check.style.opacity = 0;
            wallet_password_pw.style.borderColor = "#000000";
            validation_flag++;
        }

        // Validate Password confirm
        if(conf_password==""||conf_password==null) {
            conf_password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Please check your password.';
            conf_password_check.style.opacity = 1;
            password_confirm_pw.style.borderColor = "#910000";
            password_confirm_pw.value = "";
        } else if(password !== conf_password) {
            conf_password_check.innerHTML = '<font class="wrong">&times;</font>&nbsp; Password does not match the confirm password.';
            conf_password_check.style.opacity = 1;
            password_confirm_pw.style.borderColor = "#910000";
            password_confirm_pw.value = "";
        } else {
            conf_password_check.style.opacity = 0;
            password_confirm_pw.style.borderColor = "#000000";
            validation_flag++;
        }

        // Move next page
        if(validation_flag == 3) {
            create_modal_content[0].style.display = "none";
            // create_modal_content[1].style.display = "block";
            $('.progressPhases').css("display","none");
            app.createKeystore(null, wallet_name_tf.value, wallet_password_pw.value);
        }
    }

    setHiddenHeaderAndFooter(true);
    console.log("intro_start");
  }