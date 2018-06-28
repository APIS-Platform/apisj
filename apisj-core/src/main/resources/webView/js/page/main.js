// sort = {"asc", "desc"}, default="asc"
function loadWalletList(sort){
    // load wallet list
    getKeyStoreDataListAllWithJson(sort);

    if(keystoreList.length > 0){

        var html = "";
        for(var i=0; i<keystoreList.length; i++){
            var balance = addDotWidthIndex(keystoreList[i].balance);
            var mineral = addDotWidthIndex(keystoreList[i].mineral);

            var balanceQuotient = balance.split(".")[0];
            var balanceRemainder = balance.split(".")[1];
            var mineralQuotient = mineral.split(".")[0];
            var mineralRemainder = mineral.split(".")[1];

            // row group header
            html = html + '<tbody>';
            html = html + '<tr class="walletList">';
            html = html + '  <td>';
            html = html + '    <img src="img/new/btn_square_check_red.png" alt="checked" id="checkedImg" />'
            html = html + '    <img src="img/new/btn_square_check_grey.png" alt="unchecked" id="uncheckedImg" />';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '    <img src="img/new/icon_circle_grey.png" alt="walletUnselected" class="walletUnselectedImg" />';
            html = html + '    <img src="img/new/icon_circle_red.png" alt="walletSelected" class="walletSelectedImg" />';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '    <div class="walletName">' + keystoreList[i].alias + '</div>';
            html = html + '    <div class="walletAddress">' + keystoreList[i].address + '</div>';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '    <div id="addressMaskingDiv">';
            html = html + '      <img src="img/new/btn_addressMasking.png" alt="addAddressMasking" id="addAddressMasking" />';
            html = html + '    </div>';
            html = html + '  </td>';
            html = html + '  <td></td>';
            html = html + '  <td>';
            html = html + '    <div id="transferBtnDiv">';
            html = html + '      <img src="img/new/btn_transfer.png" alt="transferBtn" id="transferBtn" />';
            html = html + '    </div>';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '     <div class="listFoldDiv">'
            html = html + '         <img src="img/new/btn_unfold.png" alt="listUnfold" class="listUnfold" />'
            html = html + '         <img src="img/new/btn_fold.png" alt="listFold" class="listFold" />'
            html = html + '     </div>'
            html = html + '  </td>'
            html = html + '</tr>';
            html = html + '</tbody>';

            // row group body
            html = html + '<tbody class="walletDetailsAll">';
            html = html + '<tr class="walletDetails">';
            html = html + '  <td></td>';
            html = html + '  <td></td>';
            html = html + '  <td>';
            html = html + '    <img src="img/new/icon_apis_coin.png" alt="APISCoinLogo" />';
            html = html + '    &nbsp;&nbsp;APIS';
            html = html + '  </td>';
            html = html + '  <td></td>';
            html = html + '  <td>';
            html = html + '    <font id="size-12">'+balanceQuotient+'.</font><font id="size-10">'+balanceRemainder+'</font>';
            html = html + '    <font class="regular" id="size-10">&nbsp;APIS</font>';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '    <font class="regular" id="noTransaction">No Transaction</font>';
            html = html + '  </td>';
            html = html + '  <td></td>';
            html = html + '</tr>';

            // row group body - mineral
            html = html + '<tr class="walletDetails">';
            html = html + '  <td></td>';
            html = html + '  <td></td>';
            html = html + '  <td>';
            html = html + '     <img src="img/Mineral.jpg" alt="APISCoinLogo" id="APISCoinLogoImg" />';
            html = html + '     &nbsp;&nbsp;MINERAL';
            html = html + '  </td>';
            html = html + '  <td></td>';
            html = html + '  <td>';
            html = html + '    <font id="size-12">'+mineralQuotient+'.</font><font id="size-10">'+mineralRemainder+'</font>';
            html = html + '    <font class="regular" id="size-10">&nbsp;MNR</font>';
            html = html + '  </td>';
            html = html + '  <td>';
            html = html + '    <font class="regular" id="noTransaction">No Transaction</font>';
            html = html + '  </td>';
            html = html + '  <td></td>';
            html = html + '</tr>';

            html = html + '</tbody>';
        }

        if (typeof($("#walletTable tbody")) != "undefined" && $("#walletTable tbody") != null){
            $("#walletTable tbody").remove();
        }
        $("#walletTable").append(html);

        uiInitWalletList();
        console.log("mainCheckListIndex : "+mainCheckListIndex);
        walletCheck(mainCheckListIndex);


        // init total balance to zero
        setTotalBalance(app.getKeyStoreTotalBalance());
        setTotalMineral(app.getKeyStoreTotalMineral());
    }else{
        //if(keystoreList.length == 0)
        if (typeof($("#walletTable tbody")) != "undefined" && $("#walletTable tbody") != null){
            $("#walletTable tbody").remove();
        }
        setTotalBalance("0");
        setTotalMineral("0");

        if(pageNames[pageNames.length-1] != "intro"){
            locationHref("intro", intro_start);
        }
    }
}

function main_start(){

    // header top select (0 is main)
    selectHeaderDiv(0);

    // load html wallet list
    loadWalletList("asc");

    // init tap
    uiInitMainTopDiv();
    uiInitMainBottomDiv();
    uiInitMainBottomNavi();

    // show header
    setHiddenHeaderAndFooter(false);

    // init wallet list
    uiInitWalletList();

    // show wallet number 0
    walletCheck(0);
}

