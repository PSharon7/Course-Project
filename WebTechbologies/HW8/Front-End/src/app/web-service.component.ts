import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})

export class WebServiceComponent {

    url : string = 'http://localhost:8081';
    ipUrl : string = 'http://ip-api.com/json';

    timer;
    mobile: boolean = false;

    zipcodesAuto : any;
    zipcodesAutoing : boolean = false;
    zip : string;
    getGeoLocation: boolean = false;

    pageInitNumber : Array<number> = [-1, 1, 2, 3, 4, 5];
    pageNumber : Array<number>;
    currentPage : number = 1;

    categoryMapping = new Map([
        ["All Categories", "-1"],
        ["Art" , "550"],
        ["Baby" , "2984"],
        ["Books" , "267"],
        ["Clothing, Shoes & Accessories" , "11450"],
        ["Computers/Tablets & Networking" , "58058"],
        ["Health & Beauty" , "26395"],
        ["Music" , "11233"],
        ["Video Games & Consoles" , "1249"]
    ]);

    searchResult : any;

    showResults : boolean = false;
    showResultTable : boolean = false;
    showWishlistTable : boolean = false;
    showInfoTable : boolean = false;

    slideResult : string = "here";
    slideWishlist : string = "";
    slideDetail : string = "left";
    slideLoader : string = "";

    showLoaderSlide : boolean = false;

    //false for result table, true for wish list
    previousShowTable : boolean = true;

    itemDetail : any;
    thisItemDetail : any;
    photoDetail : any;
    shippingDetail : any;
    similarItemDetail : any;

    getItemDetailId : string = "";

    constructor(private http: HttpClient) {}



    async getGeoLocationFunc() {
        let response = await this.http.get(this.ipUrl).toPromise();
        this.zip = response['zip'];

        if (this.zip != null){
            this.getGeoLocation = true;
        }

        // this.getGeoLocation = false;
        // this.zip = null;

        console.log(this.getGeoLocation);
        console.log(this.zip);
        return this.zip;
    }

    /*
     * Auto Complete
     */
    async getZipCode(zip: string) {
        try {
            let response = await this.http.get(this.url + '/zipAutoComplete' + '?' + 'zip=' + zip).toPromise();
            this.zipcodesAuto = response;

            return response;
        }
        catch (e) {
            console.log(e);
        }
    }

    /*
     * Search Result
     */
    async getSearchResult(
        keyword: string,
        category: string,
        c_new: boolean, c_used: boolean, c_unspecified: boolean,
        pickup: boolean, shipping: boolean,
        distance: string,
        zipcode: string) {

        try {
            if ( distance === "") {
                distance = "10";
            }

            if ( zipcode === "") {
                zipcode = this.zip;
            }

            this.searchResult = await this.http.get(this.url + '/searchProduct' + '?' +
                'keyword=' + encodeURI(keyword) +
                '&category=' + this.categoryMapping.get(category) +
                '&new=' + c_new + '&used=' + c_used + '&unspecified=' + c_unspecified +
                '&pickup=' + pickup + '&shipping=' + shipping +
                '&distance=' + distance +
                '&zipcode=' + zipcode
            ).toPromise();

            console.log(this.searchResult);

            this.pageNumber = this.pageInitNumber.slice(1, this.searchResult.searchResult.length);
            this.showResults = true;

            this.currentPage = 1;

            this.previousShowTable = true;
            this.showResultTable = true;
            this.showWishlistTable = this.showInfoTable = false;

            this.getItemDetailId = "";


        }
        catch (e) {
            console.log(e);
        }
    }

    /*
     * Item Detail
     */
    async getItemDetail(itemId: string, item: any) {
        try {

            //Slide Animation
            this.slideLoader = "here";
            this.showLoaderSlide = true;
            this.slideResult = "right";
            this.slideDetail = "";

            this.itemDetail = await this.http.get(this.url + '/itemDetail' + '?' + 'itemId=' + itemId).toPromise();
            this.thisItemDetail = item;

            this.getItemDetailId = this.itemDetail.item.itemId;

            console.log(this.itemDetail);

            this.previousShowTable = this.showResultTable;
            this.showResults = false;

            this.showInfoTable = true;
            this.showResultTable = this.showWishlistTable = false;

            await this.getPhotoDetail(this.itemDetail.item.title);

            this.getShippingDetail(itemId);

            await this.getSimilarItemDetail(itemId);


            this.showLoaderSlide = false;
        }
        catch (e) {
            console.log(e);
        }
    }

    /*
     * Photo Detail
     */
    async getPhotoDetail(title: string) {
        try {
            this.photoDetail = await this.http.get(this.url + '/photoDetail' + '?' + 'title=' + encodeURI(title)).toPromise();

            console.log(this.photoDetail);

        }
        catch (e) {
            console.log(e);
        }
    }

    /*
     * Shipping Detail
     */
    getShippingDetail(itemId: string) {

        let result = this.searchResult.searchResult;
        for(let items in result) {
            for(let item in result[items]) {
                if (result[items][item].itemId === itemId) {
                    this.shippingDetail = result[items][item].shippingInfo;
                    this.shippingDetail['returnsAccepted'] = result[items][item].returnsAccepted;

                    console.log(this.shippingDetail);
                    return;
                }
            }
        }
    }

    /*
     * Similar Item Detail
     */
    async getSimilarItemDetail(itemId: string) {
        try {

            this.similarItemDetail = await this.http.get(this.url + '/similarItemDetail' + '?' + 'itemId=' + itemId).toPromise();

            console.log(this.similarItemDetail);

        }
        catch (e) {
            console.log(e);
        }
    }

    delayShowList() {

        this.slideDetail = "right";
        this.slideResult = this.slideWishlist = "left";

        if(this.timer) {
            clearTimeout(this.timer);
        }
        this.timer = setTimeout(this.showList.bind(this), 1000);

    }

    showList() {

        this.showResults = true;
        this.slideResult = this.slideWishlist = "here";

        if (this.previousShowTable) {
            this.showResultTable = true;
            this.showWishlistTable = this.showInfoTable = false;
        }
        else {
            this.showWishlistTable = true;
            this.showResultTable = this.showInfoTable = false;
        }
    }

    delayShowDetail() {

        this.slideResult = "left";

        if(this.timer) {
            clearTimeout(this.timer);
        }
        this.timer = setTimeout(this.showDetail.bind(this), 1000);

    }

    showDetail() {
        this.showResults = false;

        this.slideDetail = "here";
        this.showInfoTable = true;

        this.previousShowTable = this.showResultTable;
        this.showResultTable = this.showWishlistTable = false;
    }

}