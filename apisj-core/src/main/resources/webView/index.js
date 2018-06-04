// Create wallet modal
let create_modal = document.getElementById('create-wallet-modal');
let create_wallet_btn = document.getElementById('create-wallet-btn');
let coins_radio = document.getElementsByName("coins");
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
// Keystore
let download_keystore = document.getElementsByClassName('download-keystore');
let download_keystore_flag = 0;
let option_modal = document.querySelectorAll('#option-modal');
let modal_option_content = document.querySelectorAll('.modal-option-content');
let option_no = document.getElementsByClassName("option-no");
let option_yes = document.getElementsByClassName("option-yes");
let option_confirm = document.getElementsByClassName("option-confirm");
// Private Key
let private_key = document.getElementById("private-key");
let phase_complete = document.getElementsByClassName("phase-complete");
let private_key_cover_img = document.getElementById("key-cover");
let private_key_uncover_img = document.getElementById("key-uncover");
let copy_key = document.getElementsByClassName("copy-key");
let copy_complete = document.getElementsByClassName("copy-complete");

// Load wallet modal
let load_modal = document.getElementById('load-wallet-modal');
let load_wallet_btn = document.getElementById('load-wallet-btn');
let load_modal_content = document.querySelectorAll('.load-modal-content');
let load_radio = document.getElementsByName("load-wallet-radio");
let load_password_pw = document.getElementById("load_wallet_password");
let load_password_check = document.getElementById("load_password_check");
let load_password = load_password_pw.value;
let select_keystore = document.getElementsByClassName("select-keystore");
let file_form_check = document.getElementById('file_form_check');
let close_file = document.getElementsByClassName("close_file");
let keystore_file_name = document.getElementsByClassName("keystore-file-name");
let keystore_file_name_font = document.getElementById('keystore-file-name');
let drag_and_drop = document.getElementsByClassName("drag-and-drop");
let fileValidationFlag = "FileException";
let load_name_tf = document.getElementById("load_wallet_name");
let load_name_check = document.getElementById("load_name_check");
let load_name = load_name_tf.value;
let option_last_confirm = document.getElementsByClassName("option-last-confirm");

// Create wallet function
create_wallet_btn.onclick = function() {
    coins_radio[0].checked = true;
    wallet_name_tf.value = '';
    wallet_password_pw.value = '';
    password_confirm_pw.value = '';
    name_check.style.opacity = 0;
    password_check.style.opacity = 0;
    conf_password_check.style.opacity = 0;
    wallet_name_tf.style.borderColor = "#000000";
    wallet_password_pw.style.borderColor = "#000000";
    password_confirm_pw.style.borderColor = "#000000";
    create_modal.style.display = "block";
    create_modal_content[0].style.display = "block";
    download_keystore_flag = 0;
    app.resetValues();
}

close[0].onclick = function() {
    create_modal.style.display = "none";
}

close[1].onclick = function() {
    create_modal_content[1].style.display = "none";
    create_modal.style.display = "none";
}

close[2].onclick = function() {
    app.deleteKeystore();
    create_modal_content[2].style.display = "none";
    create_modal.style.display = "none";
}

close[3].onclick = function() {
    app.deleteKeystore();
    create_modal_content[3].style.display = "none";
    create_modal.style.display = "none";
}

for(let i=0; i<phase_next.length; i++) {
    phase_next[i].onmouseover = function() {
        phase_next[i].style.background = "#801925";
    }

    phase_next[i].onmouseout = function() {
        phase_next[i].style.background = "#a72130";
    }
}

for(let i=0; i<phase_back.length; i++) {
    phase_back[i].onmouseover = function() {
        phase_back[i].style.background = "#808080";
    }

    phase_back[i].onmouseout = function() {
        phase_back[i].style.background = "#262626";
    }
}

phase_next[0].onclick = function() {
    create_modal_content[0].style.display = "none";
    create_modal_content[1].style.display = "block";
}

wallet_name_tf.onkeyup = function() {
    name = wallet_name_tf.value;
 
    if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
        name_check.innerHTML = 'Enter new wallet name.(*no space with first and end letters)';
        name_check.style.opacity = 1;
        wallet_name_tf.style.borderColor = "#FF0000";
    } else {
        name_check.style.opacity = 0;
        wallet_name_tf.style.borderColor = "#000000";
    }
}

wallet_password_pw.onkeyup = function() {
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
        password_check.innerHTML = 'Please Enter your password.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
    } else if(password.length<8) {
        password_check.innerHTML = 'Password must contain at least 8 characters.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
    } else if(password_validate.length !== 3) {
        password_check.innerHTML = 'PW must contain a combination of letters, numbers, and special characters.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
    } else {
        password_check.style.opacity = 0;
        wallet_password_pw.style.borderColor = "#000000";
    }
}

password_confirm_pw.onkeyup = function() {
    password = wallet_password_pw.value;
    conf_password = password_confirm_pw.value;

    if(conf_password==""||conf_password==null) {
        conf_password_check.innerHTML = 'Please check your password.';
        conf_password_check.style.opacity = 1;
        password_confirm_pw.style.borderColor = "#FF0000";
    } else if(password !== conf_password) {
        conf_password_check.innerHTML = 'Password does not match the confirm password.';
        conf_password_check.style.opacity = 1;
        password_confirm_pw.style.borderColor = "#FF0000";
    } else {
        conf_password_check.style.opacity = 0;
        password_confirm_pw.style.borderColor = "#000000";
    }
}

phase_back[0].onclick = function() {
    create_modal_content[1].style.display = "none";
    create_modal_content[0].style.display = "block";
}

phase_next[1].onclick = function() {
    name = wallet_name_tf.value;
    password = wallet_password_pw.value;
    conf_password = password_confirm_pw.value;
    let validation_flag = 0;

    // Validate Name
    if(name == "" || name == null || name.charAt(0) == " " || name.charAt(name.length-1) == " ") {
        name_check.innerHTML = 'Enter new wallet name.';
        name_check.style.opacity = 1;
        wallet_name_tf.style.borderColor = "#FF0000";
        wallet_name_tf.value = "";
        validation_flag = 1;
    } else {
        name_check.style.opacity = 0;
        wallet_name_tf.style.borderColor = "#000000";
        validation_flag = 0;
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
        password_check.innerHTML = 'Please Enter your password.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
        wallet_password_pw.value = "";
        validation_flag = 1;
    } else if(password.length<8) {
        password_check.innerHTML = 'Password must contain at least 8 characters.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
        wallet_password_pw.value = "";
        validation_flag = 1;
    } else if(password_validate.length !== 3) {
        password_check.innerHTML = 'Password must contain a combination of letters, numbers, and special characters.';
        password_check.style.opacity = 1;
        wallet_password_pw.style.borderColor = "#FF0000";
        wallet_password_pw.value = "";
        validation_flag = 1;
    } else {
        password_check.style.opacity = 0;
        wallet_password_pw.style.borderColor = "#000000";
        validation_flag = 0;
    }

    // Validate Password confirm
    if(conf_password==""||conf_password==null) {
        conf_password_check.innerHTML = 'Please check your password.';
        conf_password_check.style.opacity = 1;
        password_confirm_pw.style.borderColor = "#FF0000";
        password_confirm_pw.value = "";
        validation_flag = 1;
    } else if(password !== conf_password) {
        conf_password_check.innerHTML = 'Password does not match the confirm password.';
        conf_password_check.style.opacity = 1;
        password_confirm_pw.style.borderColor = "#FF0000";
        password_confirm_pw.value = "";
        validation_flag = 1;
    } else {
        conf_password_check.style.opacity = 0;
        password_confirm_pw.style.borderColor = "#000000";
        validation_flag = 0;
    }

    // Move next page
    if(validation_flag == 0) {
        create_modal_content[1].style.display = "none";
        create_modal_content[2].style.display = "block";
        app.generateKeystore(wallet_password_pw.value);
    }
}

download_keystore[0].onclick = function() {
    download_keystore_flag = 1;
    app.downloadKeystore();
    option_modal[0].style.display = "block";
    modal_option_content[1].style.display = "block";
}

phase_back[1].onclick = function() {
    create_modal_content[2].style.display = "none";
    create_modal_content[1].style.display = "block";
    app.deleteKeystore();
    app.resetValues();
}

phase_next[2].onclick = function() {
    if(download_keystore_flag == 0) {
        option_modal[0].style.display = "block";
        modal_option_content[0].style.display = "block";
    } else {
        let private_key_cover = '';

        for(let i=0; i<app.getPrivateKey().length; i++) {
            private_key_cover += '*';
        }
        
        private_key.value = private_key_cover;

        create_modal_content[2].style.display = "none";
        create_modal_content[3].style.display = "block";
    }
}

option_no[0].onclick = function() {
    option_modal[0].style.display = "none";
    modal_option_content[0].style.display = "none";
}

option_yes[0].onclick = function() {
    let private_key_cover = '';

    for(let i=0; i<app.getPrivateKey().length; i++) {
        private_key_cover += '*';
    }
    
    private_key.value = private_key_cover;

    option_modal[0].style.display = "none";
    modal_option_content[0].style.display = "none";
    create_modal_content[2].style.display = "none";
    create_modal_content[3].style.display = "block";
}

option_confirm[0].onclick = function() {
    option_modal[0].style.display = "none";
    modal_option_content[1].style.display = "none";
}

private_key_uncover_img.onclick = function() {
    let private_key_cover = '';

    for(let i=0; i<app.getPrivateKey().length; i++) {
        private_key_cover += '*';
    }
    
    private_key.value = private_key_cover;

    private_key_uncover_img.style.display = "none";
    private_key_cover_img.style.display = "block";
}

private_key_cover_img.onclick = function() {
    private_key.value = app.getPrivateKey();
    private_key_cover_img.style.display = "none";
    private_key_uncover_img.style.display = "block";
}

copy_key[0].onclick = function() {
    copy_complete[0].style.display = "block";

    let private_key_copy = document.createElement("INPUT");
    private_key_copy.setAttribute("type", "text");
    private_key_copy.setAttribute("value", app.getPrivateKey());
    document.body.appendChild(private_key_copy);
    private_key_copy.select();
    document.execCommand('copy');
    document.body.removeChild(private_key_copy);
    
    setTimeout(function() {copy_complete[0].style.display = "none"}, 2000);
}

phase_complete[0].onclick = function() {
    create_modal_content[3].style.display = "none";
    create_modal.style.display = "none";
    // location.href = "main.html";
}

// Load wallet function
load_wallet_btn.onclick = function() {
    keystore_file_name[0].style.display = "none";
    drag_and_drop[0].style.borderColor = "#C4C5C6";
    file_form_check.style.opacity = 0;
    load_password_pw.value = '';
    load_name_tf.value = '';
    load_password_check.style.opacity = 0;
    load_name_check.style.opacity = 0;
    load_password_pw.style.borderColor = "#000000";
    load_name_tf.style.borderColor = "#000000";
    load_modal.style.display = "block";
    load_modal_content[0].style.display = "block";
}

close[4].onclick = function() {
    load_modal.style.display = "none";
}

close[5].onclick = function() {
    load_modal_content[1].style.display = "none";
    load_modal.style.display = "none";
}

close[6].onclick = function() {
    load_modal_content[2].style.display = "none";
    load_modal.style.display = "none";
}

phase_next[3].onclick = function() {
    if(load_radio[0].checked) {
        load_modal_content[0].style.display = "none";
        load_modal_content[1].style.display = "block";
    } else {
        location.href = "main.html";
    }
}

load_password_pw.onkeyup = function() {
    load_password = load_password_pw.value;
 
    if(load_password == "" || load_password == null) {
        load_password_check.innerHTML = 'Please enter your password.';
        load_password_check.style.opacity = 1;
        load_password_pw.style.borderColor = "#FF0000";
    } else {
        load_password_check.style.opacity = 0;
        load_password_pw.style.borderColor = "#000000";
    }
}

phase_next[4].onclick = function() {
    load_password = load_password_pw.value;
 
    if(load_password == "" || load_password == null) {
        load_password_check.innerHTML = 'Please enter your password.';
        load_password_check.style.opacity = 1;
        load_password_pw.style.borderColor = "#FF0000";
    } else if((!app.matchPassword(load_password)) || (fileValidationFlag != "CorrectFileForm")) {
        load_password_check.innerHTML = 'Please check your password.';
        load_password_check.style.opacity = 1;
        load_password_pw.style.borderColor = "#FF0000";
    } else {
        load_modal_content[1].style.display = "none";
        load_modal_content[2].style.display = "block";
    }
}

select_keystore[0].onclick = function() {
    fileValidationFlag = app.fileRead();

    if(fileValidationFlag == "CorrectFileForm") {
        keystore_file_name_font.innerHTML = app.getFileName();
        keystore_file_name[0].style.display = "block";
        keystore_file_name[0].style.background = "#a72130";
        drag_and_drop[0].style.borderColor = "#a72130";
        file_form_check.style.opacity = 0;
    } else if(fileValidationFlag == "IncorrectFileForm") {
        keystore_file_name_font.innerHTML = app.getFileName();
        keystore_file_name[0].style.display = "block";
        keystore_file_name[0].style.background = "#FF0000";
        drag_and_drop[0].style.borderColor = "#FF0000";
        file_form_check.style.opacity = 1;
    } else if(fileValidationFlag == "FileException") {
        keystore_file_name[0].style.display = "none";
        drag_and_drop[0].style.borderColor = "#C4C5C6";
        file_form_check.style.opacity = 0;
    } else {
        app.errorPopup();
    }
}

close_file[0].onclick = function() {
    fileValidationFlag == "FileException";
    keystore_file_name[0].style.display = "none";
    drag_and_drop[0].style.borderColor = "#C4C5C6";
    file_form_check.style.opacity = 0;
}

load_name_tf.onkeyup = function() {
    load_name = load_name_tf.value;
 
    if(load_name == "" || load_name == null) {
        load_name_check.innerHTML = 'Enter new wallet name.';
        load_name_check.style.opacity = 1;
        load_name_tf.style.borderColor = "#FF0000";
    } else {
        load_name_check.style.opacity = 0;
        load_name_tf.style.borderColor = "#000000";
    }
}

phase_next[5].onclick = function() {
    load_name = load_name_tf.value;
 
    if(load_name == "" || load_name == null) {
        load_name_check.innerHTML = 'Enter new wallet name.';
        load_name_check.style.opacity = 1;
        load_name_tf.style.borderColor = "#FF0000";
    } else {
        option_modal[1].style.display = "block";
        modal_option_content[2].style.display = "block";
    }
}

option_last_confirm[0].onclick = function() {
    option_modal[1].style.display = "none";
    modal_option_content[2].style.display = "none";
    load_modal_content[2].style.display = "none";
    load_modal.style.display = "none";
    location.href = "main.html";
}

// **modal off when window click
    // window.onclick = function(event) {
    //     if(event.target == modal) {
    //         modal.style.display = "none";
    //     }
    // }