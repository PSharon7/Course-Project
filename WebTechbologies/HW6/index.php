<?php
$keyword = $category = $location = $here = $itemid = $distance = "";
$conditions = $shipping = array();
$check = "checked";
$results = $response = $responseItem = $responseSimilarItem = "";

$categoryID = array(
    "All Categories" => "-1",
    "Art" => "550",
    "Baby" => "2984",
    "Books" => "267",
    "Clothing, Shoes & Accessories" => "11450",
    "Computers/Tablets & Networking" => "58058",
    "Health & Beauty" => "26395",
    "Music" => "11233",
    "Video Games & Consoles" => "1249"
);

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    setVariable();
    if (!empty($_POST['itemid'])) {
        getItemDetail($itemid);
        getSimilarItem($itemid);
    }
    else {
        search_product(urlencode($keyword), $categoryID[$category], $conditions, $shipping, $location, $distance);
    }
}

function resetVariable() {
    global $keyword, $category, $conditions, $shipping, $distance, $check, $here, $location, $itemid, $results;

    $keyword = $category = $location = $here = $itemid = "";
    $conditions = $shipping = array();
    $distance = "";
    $check = "checked";
    $results = $response = $responseItem = $responseSimilarItem = "";
}

function setVariable(){
    global $keyword, $category, $conditions, $shipping, $distance, $check, $here, $location, $itemid;
    $keyword = $_POST['keyword'];
    $category = $_POST['category'];
    if(!empty($_POST['conditions'])) {
        $conditions = $_POST['conditions'];
    }

    if(!empty($_POST['shipping'])) {
        $shipping = $_POST['shipping'];
    }

    if(!empty($_POST['enable-nearby'])) {
        if(!empty($_POST['distance'])) {
            $distance = $_POST['distance'];
        }
        else {
            $distance = 10;
        }

        if(!empty($_POST['here'])) {
            $check = "checked";
            $here = $_POST['here'];
            $location = $_POST['here'];
        } else {
            $check = "not checked";
            $location = $_POST['zipcode_value'];
        }
    }

    if (!empty($_POST['itemid'])){
        $itemid = $_POST['itemid'];
    }
}

function search_product($kw, $ctg, $cond, $ship, $loc, $dis){
    //error_reporting(E_ALL);  // Turn on all errors, warnings and notices for easier debugging
    error_reporting(0);

    $apical = "BASEURL";
    $apical .= "&keywords=$kw";
    $index = 0;

    global $results;

    if(!empty($loc)) {
        if(!preg_match('/^[0-9]{5}$/', $loc)){
            $results = "<div class='notFound'>Zipcode is invalid</div>";
            return;
        }

        $apical .= "&buyerPostalCode=" . $loc;
        $apical .= "&itemFilter($index).name=MaxDistance&itemFilter($index).value=" . $dis;
        $index++;
    }

    if($ctg != -1) {
        $apical .= "&categoryId=$ctg";
    }

    $ship_cnt = count($ship);
    if($ship_cnt == 0 || $ship_cnt == 2) {
        $apical .= "&itemFilter($index).name=FreeShippingOnly&itemFilter($index).value=true";
        $index++;
        $apical .= "&itemFilter($index).name=LocalPickupOnly&itemFilter($index).value=true";
        $index++;
    }
    else {
        foreach ($ship as $s){
            if($s == "free-shipping"){
                $apical .= "&itemFilter($index).name=FreeShippingOnly&itemFilter($index).value=true";
                $index++;
            }
            else if($s == "local-pickup"){
                $apical .= "&itemFilter($index).name=LocalPickupOnly&itemFilter($index).value=true";
                $index++;
            }
        }
    }

    $apical .= "&itemFilter($index).name=HideDuplicateItems&itemFilter($index).value=true";
    $index++;

    $cond_cnt = count($cond);
    if($cond_cnt == 0 || $cond_cnt == 3) {
        $apical .= "&itemFilter($index).name=Condition&itemFilter($index).value(0)=New&itemFilter($index).value(1)=Used&itemFilter($index).value(2)=Unspecified";
    }
    else {
        $apical .= "&itemFilter($index).name=Condition";
        $i = 0;
        foreach ($cond as $c){
            $apical .= "&itemFilter($index).value($i)=".$c;
            $i++;
        }
    }

//    echo $apical;

    // Load the call and capture the document returned by eBay API
    global $response;
    $response = json_decode(file_get_contents($apical));
    $response = json_encode($response);


}

/**
 * get Single Item when link is clicked
 */

function getItemDetail($itemId){
    //error_reporting(E_ALL);  // Turn on all errors, warnings and notices for easier debugging
    error_reporting(0);

    $apical = "BASEURL";

    global $responseItem;
    $responseItem = json_decode(file_get_contents($apical), true);

    if(!empty($responseItem['Ack']) && $responseItem['Ack'] == "Success") {

        if(!empty($responseItem['Item']) && !empty($responseItem['Item']['Description'])){
            $iframeRelated = $responseItem['Item']['Description'];
            file_put_contents("iframe.html", $iframeRelated);
        }
    }

    $responseItem = json_encode($responseItem);

}

/**
 * get Similar Item when img is clicked
 */
function getSimilarItem($itemId){
    //error_reporting(E_ALL);  // Turn on all errors, warnings and notices for easier debugging
    error_reporting(0);

    $apical = "BASEURL";

    global $responseSimilarItem;
    $responseSimilarItem = json_decode(file_get_contents($apical));
    $responseSimilarItem = json_encode($responseSimilarItem);

}

?>

<!DOCTYPE html>
<html>
<head>
    <title>HW6</title>
    <meta charset="utf-8"/>

    <style type="text/css">
        html, body {
            font-family: Times, serif;
        }
        .center {
            text-align: center;
        }

        .gray-form {
            background-color: #faf9fa;
            position: relative;
            margin: 20px auto 0;
            height: 280px;
            width: 600px;
            border: solid #c6c4c6;
        }

        .form-title {
            font-style: italic;
            font-size: 30px;
        }

        hr {
            color: #c6c4c6;
            margin-inline-start: 8px;
            margin-inline-end: 8px;
        }

        .search-item {
            text-align: left;
            padding-left: 20px;
            line-height: 30px;
        }

        .keyword-name {
            font-weight: bold;
        }

        .search-checkbox-condition {
            margin-left: 21px;
        }

        .search-checkbox-shipping {
            margin-left: 42px;
        }

        .search-disable {
            display: inline;
            pointer-events: none;
            opacity: 0.5;
        }

        .search-checkbox-distance {
            margin-left: 25px;
        }

        .miles {
            display: inline-table;
        }

        .search-button {
            text-align: left;
            padding-top: 10px;
            padding-left: 223px;
        }

        .search-button input {
            padding-left: 4px;
            padding-right: 4px;
        }

        .search-nearby {
            display: inline;
        }

        .search-disable {
            pointer-events: none;
            opacity: 0.5;
        }

        /*
            result related
        */
        table {
            position: relative;
        }

        .tableProduct {
            width: 1200px;
            margin: 20px auto 0;
        }

        .tableProduct td{
            text-align: left;
        }

        .itemHeader {
            font-weight: bold;
            font-size: 35px;
            padding-top: 20px;
        }

        .tableItem {
            margin: 5px auto 30px;
        }

        .tableItem td {
            padding-inline-start: 8px;
            padding-inline-end: 8px;
        }

        table, th, td {
            border: 2px solid #c6c4c6;
            border-collapse: collapse;
        }

        td {
            vertical-align: middle;
        }

        td img{
            position: relative;
            vertical-align: top;
        }

        .productImg{
            width: 80px !important;
        }

        .itemImg{
            height: 190px !important;
        }

        td a {
            text-decoration: none;
            color: black;
        }

        td a:hover {
            color: #909090;
        }

        .similarItemLink {
            display: block;
            text-decoration: none;
            color: black;
            width: 164px;
            font-size: 15px;
        }

        .similarItemLink:hover {
            color: #909090;
        }

        .notFound {
            border: 2px solid #c6c4c6;
            background-color: #f0f0f0;
            position: relative;
            margin: 20px auto 0;
            width: 800px;
        }

        .notFoundSimilar {
            position: relative;
            margin: 11px;
            width: 800px;
            font-size: 22px;
            font-weight: bold;
            border: 2px solid #c6c4c6;
        }

        .clickInfo {
            color: #8e8e8e;
            padding-top: 10px;
        }

        .arrow {
            display: block;
            margin: auto;
            width: 50px;
            height: 25px;
            padding: 10px 0;
        }

        .frameHtml {
            position: relative;
            margin: 0 auto;
            width: 70%;
        }

        .similarItemTable {
            position: relative;
            margin: auto auto 30px auto;
            /*height:290px;*/
            width: 800px;
            height: auto;
            border: 2px solid #c6c4c6;
            overflow-x: auto;
            overflow-y: hidden;
        }

        .similarItemInfo {
            display: table-cell;
            position: relative;
            margin-top: 0;
            margin-bottom: 0;
            padding: 20px 30px;
            vertical-align: bottom;
        }

        .price {
            font-weight: bold;
            display: block;
            padding-top: 10px;
        }


    </style>
</head>

<body class="center">
    <div class="gray-form">
        <span class="form-title">Product Search</span>
        <hr>

        <form id="myForm" name="search-form" method="post" action="<?php echo htmlentities($_SERVER['PHP_SELF']); ?>">
            <div class="search-item">
                <span class="keyword-name">Keyword</span>
                <input type="text" id="keyword" name="keyword" required="required" value="<?php echo $keyword ?>">
            </div>

            <div class="search-item">
                <span class="keyword-name">Category</span>
                <select name="category" id="category">
                    <option value="All Categories" <?php if(isset($category) && $category=="All Categories") echo 'selected';?> >All Categories</option>
                    <option value="Art" <?php if(isset($category) && $category=="Art") echo 'selected';?> >Art</option>
                    <option value="Baby" <?php if(isset($category) && $category=="Baby") echo 'selected';?> >Baby</option>
                    <option value="Books" <?php if(isset($category) && $category=="Books") echo 'selected';?> >Books</option>
                    <option value="Clothing" <?php if(isset($category) && $category=="Clothing") echo 'selected';?> >Clothing</option>
                    <option value="Shoes & Accessories" <?php if(isset($category) && $category=="Shoes & Accessories") echo 'selected';?> >Shoes & Accessories</option>
                    <option value="Computers/Tablets & Networking" <?php if(isset($category) && $category=="Computers/Tablets & Networking") echo 'selected';?> >Computers/Tablets & Networking</option>
                    <option value="Health & Beauty" <?php if(isset($category) && $category=="Health & Beauty") echo 'selected';?> >Health & Beauty</option>
                    <option value="Music and Video Games & Consoles" <?php if(isset($category) && $category=="Music and Video Games & Consoles") echo 'selected';?> >Music and Video Games & Consoles</option>
                </select>
            </div>

            <div class="search-item">
                <span class="keyword-name">Condition</span>
                <input class="search-checkbox-condition" type="checkbox" name="conditions[]" value="New" <?php if(isset($conditions) && in_array("New", $conditions)) echo " checked='checked'"?> >New
                <input class="search-checkbox-condition" type="checkbox" name="conditions[]" value="Used" <?php if(isset($conditions) && in_array("Used", $conditions)) echo " checked='checked'"?> >Used
                <input class="search-checkbox-condition" type="checkbox" name="conditions[]" value="Unspecified" <?php if(isset($conditions) && in_array("Unspecified", $conditions)) echo " checked='checked'"?> >Unspecified
            </div>

            <div class="search-item">
                <span class="keyword-name">Shipping Options</span>
                <input class="search-checkbox-shipping" type="checkbox" name="shipping[]" value="local-pickup" <?php if(isset($shipping) && in_array("local-pickup", $shipping)) echo " checked='checked'"?> >Local Pickup
                <input class="search-checkbox-shipping" type="checkbox" name="shipping[]" value="free-shipping" <?php if(isset($shipping) && in_array("free-shipping", $shipping)) echo " checked='checked'"?> >Free Shipping
            </div>

            <div class="search-item">
                <input id="enable-nearby" type="checkbox" name="enable-nearby" value="enable-nearby" <?php if(isset($_POST['enable-nearby'])) echo "checked='checked'"?> >
                <span class="keyword-name">Enable Nearby Search</span>

                <div id="enable-nearby-info" class="search-nearby search-disable">
                    <input class="search-checkbox-distance" id="distance" type="text" placeholder="10" name="distance" size="7" value="<?php echo $distance; ?>">
                    <span class="keyword-name">miles from</span>
                    <div class="miles">
                        <input type="radio" id="here" name="here" value="" <?php if(isset($_POST['here'])) echo "checked='checked'"?> >Here
                        <br>
                        <input type="radio" id="enable-zipcode" name="zipcode" >
                        <input type="text" id="zip" name="zipcode_value" placeholder="zip code" disabled>
                    </div>
                </div>
            </div>

            <div class="hidden-item">
                <input type="hidden" id="itemid" name="itemid" value="">
            </div>

            <div class="search-button">
                <input type="submit" id="search-button" name="btnSubmit" value="Search" disabled>
                <input type="button" id="clear-button" name="clear" value="Clear" onclick="clearAll();" >
            </div>
        </form>
    </div>
    <div id="results">
        <?php echo $results ?>
    </div>
</body>

<script>
    var frameHtml;

    //nearby related
    var nearby = document.getElementById('enable-nearby');
    var nearby_info = document.getElementById('enable-nearby-info');

    //here relared
    var here = document.getElementById('here');
    var zipcode = document.getElementById('zip');
    var enable_zipcode = document.getElementById('enable-zipcode');

    window.addEventListener("load", nearby_check);
    nearby.addEventListener("change", nearby_check);

    function nearby_check() {
        if (nearby.checked === true){
            nearby_info.classList.remove("search-disable");
            if (zipcode.disabled === false) {
                zipcode.required = true;
            }
        } else {
            nearby_info.classList.add("search-disable");
            zipcode.required = false;
        }
    }

    here.onchange = function () {
        if (here.checked === true){
            zipcode.disabled = true;
            enable_zipcode.checked = false;
            zipcode.value = "";
        }
    };

    enable_zipcode.onchange = function () {
        if(enable_zipcode.checked === true){
            here.checked = false;
            zipcode.disabled = false;
            zipcode.required = true;
        }
    };
    
    window.addEventListener("load", here_check);
    function here_check() {
        var check = "<?php echo $check?>";
        if(check === "checked"){
            here.checked = true;
            enable_zipcode.checked = false;
            zipcode.disabled = true;
            zipcode.value = "";
        }
        else {
            here.checked = false;
            enable_zipcode.checked = true;
            zipcode.disabled = false;
            zipcode.required = true;
            zipcode.value = "<?php echo $location?>";
        }
    }

    function enable_button() {
        document.getElementById('search-button').disabled = false;
    }

    //location related
    function getLocation() {
        try
        {
            var url = "http://ip-api.com/json";
            if (window.XMLHttpRequest)
            {
                // code for IE7+, Firefox, Chrome, Opera, Safari
                xmlhttp = new XMLHttpRequest();
            }
            else
            {
                // code for IE6, IE5
                xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            xmlhttp.open("GET", url, false);
            xmlhttp.send();
            if (xmlhttp.status === 0 || xmlhttp.status === 200)
            {
                json = JSON.parse(xmlhttp.responseText);
                here.value = json.zip;
                enable_button();
            }
            else
            {
                window.alert("IP-API json do not GET");
            }
        }
        catch (Error)
        {
            window.alert("IP-API json do not GET");
        }
    }
    getLocation();


    function parseSearchResult() {
        var json = JSON.stringify(<?php echo $response;?>);
        if(json !== JSON.stringify()) {
            var response = JSON.parse(json);
            var response_state = response.findItemsAdvancedResponse[0].ack[0];
            var result = "";
            if (response_state === "Success") {
                if (response.findItemsAdvancedResponse[0].searchResult[0]["@count"] === "0") {
                    result = "<div class='notFound'>No Records has been found</div>";
                }
                else {
                    result = "<table class='tableProduct'><tr><th>Index</th><th width='80px'>Photo</th><th>Name</th><th>Price</th><th>Zip code</th><th>Condition</th><th>Shipping Option</th></tr>";

                    var items = response.findItemsAdvancedResponse[0].searchResult[0].item;
                    var i = 0;
                    items.forEach(function (item) {
                        var itemid = item.itemId[0];
                        var index = ++i;

                        var photo = "";
                        if (item.hasOwnProperty('galleryURL') && item.galleryURL.length > 0) {
                            photo = item.galleryURL[0];
                        }

                        var name = "N/A";
                        if (item.hasOwnProperty('title') && item.title.length > 0) {
                            name = item.title[0];
                        }

                        //var currency = $item['sellingStatus'][0]['currentPrice'][0]['@currencyId'];
                        // var currency = "$";
                        var price = "N/A";
                        if(item.hasOwnProperty("sellingStatus") && item.sellingStatus.length > 0 && item.sellingStatus[0].hasOwnProperty("currentPrice") && item.sellingStatus[0].currentPrice.length > 0) {
                            var currentPrice = item.sellingStatus[0].currentPrice[0];
                            if(currentPrice.hasOwnProperty("@currencyId") && currentPrice.hasOwnProperty("__value__")){
                                price = "$" + currentPrice.__value__;
                            }
                        }

                        var zipcode = "N/A";
                        if(item.hasOwnProperty("postalCode") && item.postalCode.length > 0) {
                            zipcode = item.postalCode[0];
                        }

                        var conditon = "N/A";
                        if (item.hasOwnProperty('condition') && item.condition.length > 0 && item.condition[0].hasOwnProperty("conditionDisplayName") && item.condition[0].conditionDisplayName.length > 0) {
                            conditon = item.condition[0].conditionDisplayName[0];
                        }

                        //$shippingOption = empty($item['shippingInfo']) ? "N/A" : floatval($item['shippingInfo'][0]['shippingServiceCost'][0]['__value__']) == 0.0 ? "Free Shipping" : $item['shippingInfo'][0]['shippingServiceCost'][0]['@currencyId'] . $item['shippingInfo'][0]['shippingServiceCost'][0]['__value__'];
                        var shippingOption = "N/A";
                        if (item.hasOwnProperty('shippingInfo') && item.shippingInfo.length > 0 && item.shippingInfo[0].hasOwnProperty('shippingServiceCost') && item.shippingInfo[0].shippingServiceCost.length > 0 && item.shippingInfo[0].shippingServiceCost[0].hasOwnProperty("__value__")) {
                            if (parseFloat(item.shippingInfo[0].shippingServiceCost[0].__value__) === 0.0) {
                                shippingOption = "Free Shipping";
                            } else {
                                shippingOption = "$" + item.shippingInfo[0].shippingServiceCost[0].__value__;
                            }
                        }

                        // For each SearchResultItem node, build a link and append it to $results
                        result += "<tr><td>" + index + "</td><td><img class='productImg' src=" + photo + "></td><td><a href='#' onclick='submit(" + itemid + ");'>" + name + "</a></td><td>" + price + "</td><td>" + zipcode + "</td><td>" + conditon + "</td><td>" + shippingOption + "</td></tr>";
                    });

                    result += "</table>";
                }

            }
            else {
                result = "<div class='notFound'>No Records has been found</div>";
            }

            document.getElementById("results").innerHTML = result;
        }
    }
    parseSearchResult();

    function parseItemResult() {
        var json = JSON.stringify(<?php echo $responseItem;?>);
        if(json !== JSON.stringify()) {
            var response = JSON.parse(json);
            var response_state = response.Ack;
            var result = "";
            if (response_state === "Success") {
                result = "<div class='itemHeader'>Item Details</div>";
                result += "<table class='tableItem' style='text-align:left;'>";
                var item = response.Item;
                result += (item.PictureURL.length > 0) ? "<tr><td><b>Photo</b></td><td><img class='itemImg' src=" + item.PictureURL[0] + "></td></tr>" : "";
                result += item.hasOwnProperty("Title") ? "<tr><td><b>Title</b></td><td>" + item.Title + "</td></tr>" : "";
                result += item.hasOwnProperty("SubTitle") ? "<tr><td><b>Subtitle</b></td><td>" + item.SubTitle + "</td></tr>" : "";
                if (item.hasOwnProperty("CurrentPrice")){
                    if(item.CurrentPrice.hasOwnProperty("Value") && item.CurrentPrice.hasOwnProperty("CurrencyID")){
                        result += "<tr><td><b>Price</b></td><td>" + item.CurrentPrice.Value + " " + item.CurrentPrice.CurrencyID + "</td></tr>";
                    }
                    else {
                        result += "N/A";
                    }
                }

                if(item.hasOwnProperty("Location") && item.hasOwnProperty("PostalCode")){
                    result += "<tr><td><b>Location</b></td><td>" + item.Location + ", " + item.PostalCode + "</td></tr>";
                } else if(item.hasOwnProperty("Location")){
                    result += "<tr><td><b>Location</b></td><td>" + item.Location + "</td></tr>";
                } else if(item.hasOwnProperty("PostalCode")){
                    result += "<tr><td><b>Location</b></td><td>" + item.PostalCode + "</td></tr>";
                }

                result += (item.hasOwnProperty("Seller") && item.Seller.hasOwnProperty("UserID")) ? "<tr><td><b>Seller</b></td><td>" + item.Seller.UserID + "</td></tr>" : "";

                if (item.hasOwnProperty("Return Policy") && item.ReturnPolicy.hasOwnProperty("ReturnsAccepted")){
                    if(item.ReturnPolicy.ReturnsAccepted === "ReturnsNotAccepted") {
                        result += "<tr><td><b>Return Policy (US)</b></td><td>Returns Not Accepted</td></tr>";
                    } else if(item.ReturnPolicy.ReturnsAccepted === "Returns Accepted") {
                        result += "<tr><td><b>Return Policy (US)</b></td><td>Returns Accepted within " + item.ReturnPolicy.ReturnsWithin + "</td></tr>";
                    }
                }

                if (item.hasOwnProperty("ItemSpecifics") && item.ItemSpecifics.NameValueList.length > 0){
                    item.ItemSpecifics.NameValueList.forEach(function (i) {
                        result += "<tr><td><b>" + i.Name + "</b></td><td>" + i.Value + "</td></tr>";
                    });
                }

                result += "</table>";

                result += "<div id='sellerInfo' class='clickInfo'><span id='sellerMsg'>click to show seller message</span><img id='forseller' class='arrow' src='http://csci571.com/hw/hw6/images/arrow_down.png'></div>";
                if (item.hasOwnProperty("Description")){
                    frameHtml = item.Description;
                    result += "<iframe class='frameHtml' id='seller' scrolling='no' frameborder='0' style='display:none;' src='iframe.html'></iframe>";
                }
                else {
                    result += "<div class='notFound'>No Seller Message found</div>";
                }
                result += "<div id='similarItem' class='clickInfo'><span id='similarMsg'>click to show similar items</span><img id='forsimilar' class='arrow' src='http://csci571.com/hw/hw6/images/arrow_down.png'></div>";
                result += "<div id='similar' class='similarItemTable' style='display:none;'></div>";
            }
            else {
                result += "<div class='notFound'>No Item Detail for this item</div>";
            }

            document.getElementById("results").innerHTML = result;
        }
    }
    parseItemResult();

    function parseSimilarItemResult() {
        var json = JSON.stringify(<?php echo $responseSimilarItem;?>);
        if(json !== JSON.stringify()) {
            var response = JSON.parse(json);
            var response_state = response.getSimilarItemsResponse.ack;
            var result = "";
            if (response_state === "Success") {
                var items = response.getSimilarItemsResponse.itemRecommendations.item;
                if(items.length === 0) {
                    // document.getElementById("similar").classList.remove("similarItemTable");
                    result += "<div class='notFoundSimilar'>No Similar Item found</div>"
                }
                else {
                    items.forEach(function (item) {
                        result += "<div class='similarItemInfo'>";
                        if (item.hasOwnProperty("imageURL")){
                            result += "<img src='" + item.imageURL + "'>";
                        }
                        itemid = item.itemId;
                        if (item.hasOwnProperty("title")){
                            result += "<a class='similarItemLink' href='#' onclick='submit(" + itemid + ");'>" + item.title + "</a>";
                        }
                        if (item.hasOwnProperty("buyItNowPrice")){
                            result += "<span class='price'>" + "$" + item.buyItNowPrice.__value__ + "</span>";
                        }
                        result += "</div>";
                    });
                }
            }
            else {
                result += "<div class='notFoundSimilar'>No Similar Item found</div>"
            }

            document.getElementById("similar").innerHTML = result;
        }
    }
    parseSimilarItemResult();

    function submit(itemid) {
        document.getElementById("itemid").value = itemid;
        document.getElementById("myForm").submit();
        return false;
    }

    function clearAll() {
        document.getElementById("myForm").reset();

        <?php resetVariable()?>

        document.getElementById("keyword").value = "";
        document.getElementById("category").value = "All Categories";
        document.getElementsByName("conditions[]").forEach(function (c) {
            c.checked = false;
        });
        document.getElementsByName("shipping[]").forEach(function (s) {
            s.checked = false;
        });

        document.getElementById("distance").value = "";

        nearby.checked = false;
        here.checked = true;
        document.getElementById("enable-zipcode").checked = false;
        document.getElementById("zip").value = "";
        document.getElementById("zip").disable = true;

        nearby_check();

        document.getElementById("results").innerHTML = "";
    }


    //for seller information and similar items information
    var sellerInfo = document.getElementById("seller");
    var similarInfo = document.getElementById("similar");
    var heightSet = false;

    function sellerInfoShow() {
        sellerInfo.style.display = "block";
        if(!heightSet){
            setHeight();
        }
    }

    function sellerInfoHide() {
        sellerInfo.style.display = "none";
    }

    function similarInfoShow() {
        similarInfo.style.display = "block";
    }

    function similarInfoHide() {
        similarInfo.style.display = "none";
    }

    var sellerMsg = document.getElementById("sellerMsg");
    var similarMsg = document.getElementById("similarMsg");

    var imgSeller = document.getElementById("forseller");
    var imgSimilar = document.getElementById("forsimilar");

    if (imgSeller !== null) {
        imgSeller.addEventListener("click", function () {
            if (imgSeller.src === 'http://csci571.com/hw/hw6/images/arrow_down.png') {
                imgSeller.src = 'http://csci571.com/hw/hw6/images/arrow_up.png';
                sellerMsg.innerHTML = "click to hide seller message";
                sellerInfoShow();
                if (imgSimilar.src === 'http://csci571.com/hw/hw6/images/arrow_up.png') {
                    imgSimilar.src = 'http://csci571.com/hw/hw6/images/arrow_down.png';
                    similarMsg.innerHTML = "click to show similar items";
                    similarInfoHide();
                }

            }
            else {
                imgSeller.src = 'http://csci571.com/hw/hw6/images/arrow_down.png';
                sellerMsg.innerHTML = "click to show seller message";
                sellerInfoHide();
            }
        });
    }

    if (imgSimilar !== null) {
        imgSimilar.addEventListener("click", function () {
            if (imgSimilar.src === 'http://csci571.com/hw/hw6/images/arrow_down.png') {
                imgSimilar.src = 'http://csci571.com/hw/hw6/images/arrow_up.png';
                similarMsg.innerHTML = "click to hide similar items";
                similarInfoShow();
                if (imgSeller.src === 'http://csci571.com/hw/hw6/images/arrow_up.png') {
                    imgSeller.src = 'http://csci571.com/hw/hw6/images/arrow_down.png';
                    sellerMsg.innerHTML = "click to show seller message";
                    sellerInfoHide();
                }
            }
            else {
                imgSimilar.src = 'http://csci571.com/hw/hw6/images/arrow_down.png';
                similarMsg.innerHTML = "click to show similar items";
                similarInfoHide();
            }
        });
    }

    function setHeight() {
        var iframe = document.getElementById("seller");
        var subWeb = document.frames ? document.frames[0].document : iframe.contentDocument;
        if(iframe != null && subWeb != null){
            var body = subWeb.body, html = subWeb.documentElement;
            iframe.height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight) + "px"
        }
        heightSet = true;
    }
</script>
</html>
