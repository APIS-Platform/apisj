function loadWalletList(){
    // load wallet list
    getKeyStoreDataListAllWithJson();

    var html = "";

    for(var i=0; i<keystoreList.length; i++){

        // row group header
        html = html + '<tbody>';
        html = html + '<tr class="walletList">';
        html = html + '  <td>';
        html = html + '    <img src="img/checked.jpg" alt="checked" id="checkedImg" />';
        html = html + '    <img src="img/unchecked.jpg" alt="unchecked" id="uncheckedImg" />';
        html = html + '  </td>';
        html = html + '  <td>';
        html = html + '    <img src="img/noneWallet.jpg" alt="nonWallet" id="noneWalletImg" />';
        html = html + '  </td>';
        html = html + '  <td>';
        html = html + '    <div class="walletName">' + keystoreList[i].alias + '</div>';
        html = html + '    <div class="walletAddress">' + keystoreList[i].address + '</div>';
        html = html + '  </td>';
        html = html + '  <td>';
        html = html + '    <span id="addressMaskingAdd">';
        html = html + '      <img src="img/plusWhite.jpg" alt="plusWhite" id="plusWhiteImg" />';
        html = html + '      Address Masking';
        html = html + '    </span>';
        html = html + '  </td>';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <span id="transferBtn">';
        html = html + '      <img src="img/rightArrowWhite.jpg" alt="rightArrowWhiteImg" id="rightArrowWhiteImg" />';
        html = html + '      Transfer';
        html = html + '    </span>';
        html = html + '  </td>';
        html = html + '  <td></td>';
        html = html + '</tr>';
        html = html + '</tbody>';

        // row group body
        html = html + '<tbody class="walletDetailsAll">';
        html = html + '<tr class="walletDetails">';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <img src="img/APISCoinLogo.jpg" alt="APISCoinLogo" id="APISCoinLogoImg" />';
        html = html + '  </td>';
        html = html + '  <td>APIS</td>';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <font id="size-12">123,456,231.</font><font id="size-11">000000000000000000</font>';
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
        html = html + '  <td>';
        html = html + '    <img src="img/Mineral.jpg" alt="APISCoinLogo" id="APISCoinLogoImg" />';
        html = html + '  </td>';
        html = html + '  <td>MINERAL</td>';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <font id="size-12">23,456,231.</font><font id="size-11">000000000000000000</font>';
        html = html + '    <font class="regular" id="size-10">&nbsp;MNR</font>';
        html = html + '  </td>';
        html = html + '  <td>';
        html = html + '    <font class="regular" id="noTransaction">No Transaction</font>';
        html = html + '  </td>';
        html = html + '  <td></td>';
        html = html + '</tr>';

        // row group body - token
        html = html + '<tr class="walletDetails">';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <img src="img/APISTokenLogo.jpg" alt="APISCoinLogo" id="APISCoinLogoImg" />';
        html = html + '  </td>';
        html = html + '  <td>APIS TOKEN</td>';
        html = html + '  <td></td>';
        html = html + '  <td>';
        html = html + '    <font id="size-12">0.</font><font id="size-11">000000000000000000</font>';
        html = html + '    <font class="regular" id="size-10">&nbsp;APIT</font>';
        html = html + '  </td>';
        html = html + '  <td>';
        html = html + '    <font class="regular" id="noTransaction">No Transaction</font>';
        html = html + '  </td>';
        html = html + '  <td></td>';
        html = html + '</tr>';
        html = html + '</tbody>';
    }

    $("#walletTable tbody").remove();
    $("#walletTable").append(html);
}

function main_start(){
    // load html wallet list
    loadWalletList();

    // init tap
    uiInitMainTopSpan();
    uiInitMainBottomSpan();

    // show header
    setHiddenHeaderAndFooter(false);

    // init wallet list
    uiInitWalletList();

    // show wallet number 0
    walletCheck(0);
}

