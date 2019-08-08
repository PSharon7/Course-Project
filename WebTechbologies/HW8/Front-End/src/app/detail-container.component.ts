import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';
import { LocalServiceComponent } from './local-service.component';

@Component({
    selector: 'detail-container',
    templateUrl: './detail-container.html',
    styleUrls: ['./detail-container.css']
})

export class DetailContainerComponent {

    activeBtnClass = "btn bg-dark text-white rounded-top";
    inactiveBtnClass = "btn bg-white rounded-top btn-hover";

    productBtnClass = this.activeBtnClass;
    photoBtnClass = this.inactiveBtnClass;
    shippingBtnClass = this.inactiveBtnClass;
    sellerBtnClass = this.inactiveBtnClass;
    similarBtnClass = this.inactiveBtnClass;

    showProductTab : boolean = true;
    showPhotoTab : boolean = false;
    showShippingTab : boolean = false;
    showSellerTab : boolean = false;
    showSimilarTab : boolean = false;

    constructor(public webService : WebServiceComponent, public localService : LocalServiceComponent) {
    }

    showProduct() {
        this.productBtnClass = this.activeBtnClass;
        this.photoBtnClass = this.shippingBtnClass = this.sellerBtnClass = this.similarBtnClass = this.inactiveBtnClass;

        this.showProductTab = true;
        this.showPhotoTab = this.showShippingTab = this.showSellerTab = this.showSimilarTab = false;
    }

    showPhoto() {
        this.photoBtnClass = this.activeBtnClass;
        this.productBtnClass = this.shippingBtnClass = this.sellerBtnClass = this.similarBtnClass = this.inactiveBtnClass;

        this.showPhotoTab = true;
        this.showProductTab = this.showShippingTab = this.showSellerTab = this.showSimilarTab = false;
    }

    showShipping() {
        this.shippingBtnClass = this.activeBtnClass;
        this.photoBtnClass = this.productBtnClass = this.sellerBtnClass = this.similarBtnClass = this.inactiveBtnClass;

        this.showShippingTab = true;
        this.showPhotoTab = this.showProductTab = this.showSellerTab = this.showSimilarTab = false;
    }

    showSeller() {
        this.sellerBtnClass = this.activeBtnClass;
        this.photoBtnClass = this.shippingBtnClass = this.productBtnClass = this.similarBtnClass = this.inactiveBtnClass;

        this.showSellerTab = true;
        this.showPhotoTab = this.showShippingTab = this.showProductTab = this.showSimilarTab = false;
    }

    showSimilar() {
        this.similarBtnClass = this.activeBtnClass;
        this.photoBtnClass = this.shippingBtnClass = this.sellerBtnClass = this.productBtnClass = this.inactiveBtnClass;

        this.showSimilarTab = true;
        this.showPhotoTab = this.showShippingTab = this.showSellerTab = this.showProductTab = false;
    }

}
