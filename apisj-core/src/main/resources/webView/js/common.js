//url list
let pageNames = [];
let pagePaths = [];
var keystoreList = [];

// <a href="">
function locationHref(openPageName, callback){
    if(openPageName && openPageName.length > 0){
        var url = "html/"+openPageName+"/"+openPageName+".html";

        pageNames[pageNames.length] = openPageName;
        pagePaths[pagePaths.length] = url;
        $.ajax({
            crossOrigin: true,
            url: url,
            success: function(data) {
                $("#page-body").html(data);
                if(callback){
                    callback();
                }
            }
        });
    }
}

// return now page url
function getPagePath(){
    return pagePaths[pagePaths.length-1];
}

function comma(num){
    var len, point, str;
    num = num + "";
    point = num.length % 3 ;
    len = num.length;

    str = num.substring(0, point);
    while (point < len) {
        if (str != "") str += ",";
        str += num.substring(point, point + 3);
        point += 3;
    }
    return str;
}

/* ==================================================
 * javascript for app (java)
 * ================================================== */
function getKeystoreListSize(){
    return app.getKeystoreListSize();
}
function getKeyStoreDataListAllWithJson(){
    var jsonData = app.getKeyStoreDataListAllWithJson();
    keystoreList = JSON.parse(jsonData);
}

// call after loading
function common_start(){

    // if(getKeystoreListSize() == 0){
    //     // keystore가 없을 경우 intro 화면으로 이동
    //     locationHref("intro", intro_start);
    // }else{
    //     // keystore가 있을 경우 main 화면으로 이동
    //     getKeyStoreDataListAllWithJson();
    //     locationHref("main", main_start);
    // }
    locationHref("intro", intro_start);
    // locationHref("main", main_start);
}