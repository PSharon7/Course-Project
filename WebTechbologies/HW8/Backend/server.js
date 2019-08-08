const express = require('express');
const bodyParser = require('body-parser');
// const request = require('request');
const http = require('http');
const https = require('https');

var app = express();

app.use(bodyParser.json());
app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    next();
});
app.use(express.static('dist'));


var router = express.Router();


router.get('/zipAutoComplete', async function (req, res) {

    let url = "";

    http.get(url, (resp) => {
        let data = '';

        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received. Print out the result.
        resp.on('end', () => {
            let postalCodes = JSON.parse(data).postalCodes;
            var codes = [];
            for(var code in postalCodes) {
                codes.push(postalCodes[code].postalCode);
            }
            res.send(codes);
        });

    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });

});


router.get('/searchProduct', async function (req, res) {

    let url = "";

    if (req.query.category !== "-1"){
        url += "&categoryId=" + req.query.category;
    }

    url += "&buyerPostalCode=" + req.query.zipcode;
    url += "&itemFilter(0).name=MaxDistance&itemFilter(0).value=" + req.query.distance;


    let index = 1;
    if (req.query.shipping === "true") {
        url += "&itemFilter(" + index + ").name=FreeShippingOnly&itemFilter(" + index + ").value=" + req.query.shipping;
        index++;
    }

    if (req.query.pickup === "true") {
        url += "&itemFilter(" + index + ").name=LocalPickupOnly&itemFilter(" + index + ").value=" + req.query.pickup;
        index++;
    }

    url += "&itemFilter(" + index + ").name=HideDuplicateItems&itemFilter(" + index + ").value=true";
    index++;

    if (req.query.new === "true" || req.query.used === "true" || req.query.unspecified === "true") {
        url += "&itemFilter(" + index + ")name=Condition";
        let i = 0;
        if (req.query.new === "true") {
            url += "&itemFilter(" + index + ").value(" + i + ")=New";
            i++;
        }
        if (req.query.used === "true") {
            url += "&itemFilter(" + index + ").value(" + i + ")=Used";
            i++;
        }
        if (req.query.unspecified === "true") {
            url += "&itemFilter(" + index + ").value(" + i + ")=Unspecified";
        }
    }

    url += "&outputSelector(0)=SellerInfo&outputSelector(1)=StoreInfo";

    console.log(url);

    http.get(url, (resp) => {
        let data = '';

        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received. Print out the result.
        resp.on('end', () => {
            // console.log(data);

            let results = {};

            let object = JSON.parse(data).findItemsAdvancedResponse[0];

            results['ack'] = object.ack[0];
            results['itemSize'] = "0";
            results['searchResult'] = [[]];

            if (object.ack[0] === "Success") {

                if (object.searchResult[0]["@count"] !== "0") {

                    results['itemSize'] = object.searchResult[0]["@count"];

                    let items = object.searchResult[0].item;
                    let count = 1;
                    let page = [];
                    for (let i in items) {
                        let item = {};

                        item['itemId'] = items[i].itemId[0];

                        item['index'] = count;
                        item['image'] = "";
                        if (items[i].hasOwnProperty('galleryURL') && items[i].galleryURL.length > 0) {
                            item['image'] = items[i].galleryURL[0];
                        }

                        item['title'] = "N/A";
                        if (items[i].hasOwnProperty('title') && items[i].title.length > 0) {
                            item['title'] = items[i].title[0];
                        }

                        item['price'] = "N/A";
                        if (items[i].hasOwnProperty("sellingStatus") && items[i].sellingStatus.length > 0 && items[i].sellingStatus[0].hasOwnProperty("currentPrice") && items[i].sellingStatus[0].currentPrice.length > 0) {
                            var currentPrice = items[i].sellingStatus[0].currentPrice[0];
                            if (currentPrice.hasOwnProperty("@currencyId") && currentPrice.hasOwnProperty("__value__")) {
                                item['price'] = "$" + currentPrice.__value__;
                            }
                        }

                        item['shipping'] = "N/A";
                        item['shippingInfo'] = [];
                        if (items[i].hasOwnProperty('shippingInfo') && items[i].shippingInfo.length > 0) {
                            item['shippingInfo'].push(items[i].shippingInfo[0]);
                            item['shippingInfo'][0]['shippingCost'] = "N/A";

                            if (items[i].shippingInfo[0].hasOwnProperty('shippingServiceCost') && items[i].shippingInfo[0].shippingServiceCost.length > 0 && items[i].shippingInfo[0].shippingServiceCost[0].hasOwnProperty("__value__")) {
                                if (parseFloat(items[i].shippingInfo[0].shippingServiceCost[0].__value__) === 0.0) {
                                    item['shipping'] = "Free Shipping";
                                    item['shippingInfo'][0]['shippingCost'] = "Free Shipping";
                                } else {
                                    item['shipping'] = "$" + items[i].shippingInfo[0].shippingServiceCost[0].__value__;
                                    item['shippingInfo'][0]['shippingCost'] = "$" + items[i].shippingInfo[0].shippingServiceCost[0].__value__;
                                }
                            }
                        }


                        item['zip'] = "N/A";
                        if (items[i].hasOwnProperty("postalCode") && items[i].postalCode.length > 0) {
                            item['zip'] = items[i].postalCode[0];
                        }

                        item['seller'] = "N/A";
                        if (items[i].hasOwnProperty("sellerInfo") && items[i].sellerInfo.length > 0 && items[i].sellerInfo[0].hasOwnProperty("sellerUserName") && items[i].sellerInfo[0].sellerUserName.length > 0) {
                            item['seller'] = items[i].sellerInfo[0].sellerUserName[0];
                        }

                        item['returnsAccepted'] = "N/A";
                        if (items[i].hasOwnProperty("returnsAccepted") && items[i].returnsAccepted.length > 0) {
                            item['returnsAccepted'] = items[i].returnsAccepted[0];
                        }

                        item['condition'] = "N/A";
                        if (items[i].hasOwnProperty("condition") && items[i].condition.length > 0 && items[i].condition[0].hasOwnProperty("conditionDisplayName") && items[i].condition[0].conditionDisplayName.length > 0) {
                            item['condition'] = items[i].condition[0].conditionDisplayName[0];
                        }

                        page.push(item);
                        if(count%10 === 0) {
                            results['searchResult'].push(page);
                            page = [];
                        }

                        count++;
                        // results['searchResult'].push(item);
                    }

                    if(count%10 !== 1) {
                        results['searchResult'].push(page);
                    }

                }
            }

            res.send(results);

        });

    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });

});


router.get('/itemDetail', async function (req, res) {
    let url = "";

    console.log(url);

    http.get(url, (resp) => {
        let data = '';

        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received. Print out the result.
        resp.on('end', () => {
            // console.log(data);

            let results = {};

            let object = JSON.parse(data);

            results['ack'] = object.Ack;
            results['item'] = {};
            results['seller'] = {};

            // hw9
            results['returnPolicy'] = {};
            results['globalShipping'] = "";
            results['conditionDescription'] = "";

            if (object.Ack === "Success") {
                let itemObject = object.Item;
                let item = {};
                item['itemId'] = itemObject.ItemID;
                item['title'] = itemObject.Title;

                item['image'] = [];
                if (itemObject.hasOwnProperty('PictureURL') && itemObject.PictureURL.length > 0) {
                    item['image'] = itemObject.PictureURL;
                }

                item['subTitle'] = "";
                if (itemObject.hasOwnProperty('Subtitle')) {
                    item['subTitle'] = itemObject.Subtitle;
                }
                item['price'] = "";
                if (itemObject.hasOwnProperty('CurrentPrice')) {
                    var currentPrice = itemObject.CurrentPrice;
                    if (currentPrice.hasOwnProperty('CurrencyID') && currentPrice.hasOwnProperty('Value')) {
                        item['price'] = "$" + currentPrice.Value;
                    }
                }

                item['location'] = "";
                if (itemObject.hasOwnProperty('Location')) {
                    item['location'] = itemObject.Location;
                }

                item['returnPolicy'] = "";
                if (itemObject.hasOwnProperty('ReturnPolicy') && itemObject.ReturnPolicy.hasOwnProperty("ReturnsAccepted")) {
                    if (itemObject.ReturnPolicy.ReturnsAccepted === "ReturnsNotAccepted") {
                        item['returnPolicy'] = "ReturnsNotAccepted";
                    }
                    else if (itemObject.ReturnPolicy.ReturnsAccepted === "Returns Accepted"){
                        if (itemObject.ReturnPolicy.hasOwnProperty("ReturnsWithin") ) {
                            item['returnPolicy'] = "Returns Accepted Within " + itemObject.ReturnPolicy.ReturnsWithin;
                        }
                        else {
                            item['returnPolicy'] = "Returns Accepted";
                        }
                    }
                }

                item['brand'] = "";
                item['itemSpecifics'] = [];
                if (itemObject.hasOwnProperty('ItemSpecifics') && itemObject.ItemSpecifics.hasOwnProperty('NameValueList') && itemObject.ItemSpecifics.NameValueList.length > 0) {
                    for (var i in itemObject.ItemSpecifics.NameValueList) {
                        if (itemObject.ItemSpecifics.NameValueList[i].Name === "Brand") {
                            if (itemObject.ItemSpecifics.NameValueList[i].Value.length > 0) {
                                item['brand'] = itemObject.ItemSpecifics.NameValueList[i].Value[0];
                            }
                        }
                        else {
                            let l = {};
                            l['name'] = itemObject.ItemSpecifics.NameValueList[i].Name;
                            l['value'] = "";
                            let value = [];
                            for (var j in itemObject.ItemSpecifics.NameValueList[i].Value) {
                                value.push(itemObject.ItemSpecifics.NameValueList[i].Value[j]);
                            }
                            l['value'] = value.join(", ");

                            item['itemSpecifics'].push(l);
                        }
                    }

                }

                item['viewItemURL'] = "";
                if(itemObject.hasOwnProperty("ViewItemURLForNaturalSearch")) {
                    item['viewItemURL'] = itemObject.ViewItemURLForNaturalSearch;
                }

                let seller = {};
                seller['userId'] = "";
                seller['feedbackScore'] = "";
                seller['popularity'] = "";
                seller['feedbackRatingStar'] = "";
                seller['topRatedSeller'] = "";
                if (itemObject.hasOwnProperty('Seller')) {
                    if(itemObject.Seller.hasOwnProperty("UserID")){
                        seller['userId'] = itemObject.Seller.UserID;
                    }
                    if(itemObject.Seller.hasOwnProperty("FeedbackScore")){
                        seller['feedbackScore'] = itemObject.Seller.FeedbackScore;
                    }
                    if(itemObject.Seller.hasOwnProperty("PositiveFeedbackPercent")){
                        seller['popularity'] = itemObject.Seller.PositiveFeedbackPercent;
                    }
                    if(itemObject.Seller.hasOwnProperty("FeedbackRatingStar")){
                        seller['feedbackRatingStar'] = itemObject.Seller.FeedbackRatingStar;
                    }
                    if(itemObject.Seller.hasOwnProperty("TopRatedSeller")){
                        seller['topRatedSeller'] = itemObject.Seller.TopRatedSeller;
                    }
                }

                seller['storeURL'] = "";
                seller['storeName'] = "";
                if (itemObject.hasOwnProperty('Storefront')) {
                    if (itemObject.Storefront.hasOwnProperty("StoreURL")) {
                        seller['storeURL'] = itemObject.Storefront.StoreURL;
                    }
                    if (itemObject.Storefront.hasOwnProperty("StoreName")) {
                        seller['storeName'] = itemObject.Storefront.StoreName;
                    }
                }


                // hw9
                if (itemObject.hasOwnProperty('ReturnPolicy')) {
                    results['returnPolicy'] = itemObject.ReturnPolicy;
                }

                if (itemObject.hasOwnProperty("GlobalShipping")) {
                    results['globalShipping'] = itemObject.GlobalShipping;
                }

                if (itemObject.hasOwnProperty("ConditionDescription")) {
                    results['conditionDescription'] = itemObject.ConditionDescription;
                }

                results['item'] = item;
                results['seller'] = seller;
            }

            res.send(results);

        });

    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });

});


router.get('/photoDetail', async function (req, res) {
    let url = "";

    https.get(url, (resp) => {
        let data = '';

        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received. Print out the result.
        resp.on('end', () => {
            // console.log(data);

            let results = {};

            let object = JSON.parse(data);
            results['size'] = 0;

            if (object.hasOwnProperty("items") && object.items.length > 0) {
                results['size'] = object.items.length;

                results['image'] = [];
                results['image0'] = [];
                results['image1'] = [];
                results['image2'] = [];
                for (let i in object.items) {
                    let img = {};
                    img['link'] = object.items[i].link;
                    if(Number(i % 3) === 0 && Number(i) !== 6){
                        results['image0'].push(img);
                    }
                    else if((Number(i % 3) === 1 && Number(i) !== 7) || Number(i) === 6 ){
                        results['image1'].push(img);
                    }
                    else{
                        results['image2'].push(img);
                    }

                    results['image'].push(img);
                }

            }

            res.send(results);

        });

    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });

});


router.get('/similarItemDetail', async function (req, res) {

    let url = "";

    console.log(url);

    http.get(url, (resp) => {
        let data = '';

        // A chunk of data has been recieved.
        resp.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received. Print out the result.
        resp.on('end', () => {
            // console.log(data);

            let results = {};

            let object = JSON.parse(data).getSimilarItemsResponse;

            results['ack'] = object.ack;
            results['item'] = [];

            if (object.ack === "Success" && object.hasOwnProperty("itemRecommendations") && object.itemRecommendations.hasOwnProperty("item") && object.itemRecommendations.item.length > 0 ) {
                let itemObject = object.itemRecommendations.item;

                for (let i in itemObject) {
                    let item = {};
                    item['itemId'] = itemObject[i].itemId;

                    item['image'] = "";
                    if (itemObject[i].hasOwnProperty("imageURL")) {
                        item['image'] = itemObject[i].imageURL;
                    }

                    item['productName'] = "";
                    if (itemObject[i].hasOwnProperty("title")) {
                        item['productName'] = itemObject[i].title;
                    }

                    item['productURL'] = "";
                    if (itemObject[i].hasOwnProperty("viewItemURL")) {
                        item['productURL'] = itemObject[i].viewItemURL;
                    }

                    item['price'] = "";
                    if (itemObject[i].hasOwnProperty("buyItNowPrice") && itemObject[i].buyItNowPrice.hasOwnProperty("__value__")) {
                        item['price'] = Number(itemObject[i].buyItNowPrice.__value__);
                    }

                    item['shippingCost'] = "";
                    if (itemObject[i].hasOwnProperty("shippingCost") && itemObject[i].shippingCost.hasOwnProperty("__value__")) {
                        item['shippingCost'] = Number(itemObject[i].shippingCost.__value__);
                    }

                    item['dayLeft'] = "";
                    if (itemObject[i].hasOwnProperty("timeLeft")) {
                        let value = itemObject[i].timeLeft;
                        item['dayLeft'] = Number(value.substring(value.indexOf("P")+1, value.indexOf("D")));
                    }

                    results['item'].push(item);
                }
            }

            res.send(results);

        });

    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });

});

app.use(router);
app.listen(8081);