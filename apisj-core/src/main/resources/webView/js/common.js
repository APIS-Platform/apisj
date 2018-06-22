//url list
let pageNames = [];
let pagePaths = [];
var keystoreList = [];

// <a href="">
function locationHref(openPageName, callback){
    if(openPageName && openPageName.length > 0){
        if(openPageName === "main") {
            $('#page-body').css("background", "none");
            $('#page-body').css("height", "610px");
            $('#statusWhite').css("display", "none");
            $('.statusBar').css("display", "block");
        }

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

// add comma in number
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

// 지갑이름을 기준으로 오름차순 정렬
function sortTypeWalletAliasAsc(a, b) {
    if(a.alias == b.alias){
        return 0;
    }
    return a.alias > b.alias ? 1 : -1;
}
// 지갑이름을 기준으로 내림차순 정렬
function sortTypeWalletAliasDesc(a, b) {
    if(a.alias == b.alias){
        return 0;
    }
    return a.alias < b.alias ? 1 : -1;
}

/* ==================================================
 * javascript for app (java)
 * ================================================== */
function getKeystoreListSize(){
    return app.getKeystoreListSize();
}
function getKeyStoreDataListAllWithJson(sort){
    var jsonData = app.getKeyStoreDataListAllWithJson();
    keystoreList = JSON.parse(jsonData);

    //이름 순으로 정렬
    if(sort.toLowerCase() == "desc"){
        keystoreList.sort(sortTypeWalletAliasDesc);
    }else{
        keystoreList.sort(sortTypeWalletAliasAsc);
    }

}

// call after loading
function common_start(){

     if(getKeystoreListSize() == 0){
         // keystore가 없을 경우 intro 화면으로 이동
         locationHref("intro", intro_start);
     }else{
         // keystore가 있을 경우 main 화면으로 이동
         locationHref("main", main_start);
     }
}
