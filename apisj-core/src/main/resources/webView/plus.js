function aPlusB(result) {
    var aPlusBTxt = document.getElementById('abSum');

    // 1. Convert type from Java transfer value
    result = Number(result);

    // 2. Validate value and show
    if(isNaN(result) || result == '') {
        aPlusBTxt.value = '';
    } else {
        aPlusBTxt.value = result;
    }
}

function cPlusD() {
    // 1. Gets the C and D values
    var c = document.getElementById('cNum').value;
    var d = document.getElementById('dNum').value;

    //2. Call back to Java method
    app.cPlusD(c,d);
}