import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';


@Component({
    selector: 'seller-tab-table',
    templateUrl: './seller-tab-table.html',
    styleUrls: ['./seller-tab-table.css']
})

export class SellerTabTableComponent {

    defaultClass = "material-icons icon";

    constructor(public webService : WebServiceComponent) {

    }


    /*
     * Get Color
     */

    getColorClass(feedbackRatingStar: Number) {
        if(feedbackRatingStar < 50) {
            return this.defaultClass + " yellow";
        }
        else if(feedbackRatingStar < 100) {
            return this.defaultClass + " blue";
        }
        else if(feedbackRatingStar < 500) {
            return this.defaultClass + " turquoise";
        }
        else if(feedbackRatingStar < 1000) {
            return this.defaultClass + " purple";
        }
        else if(feedbackRatingStar < 5000) {
            return this.defaultClass + " red";
        }
        else if(feedbackRatingStar < 10000) {
            return this.defaultClass + " green";
        }
        else if(feedbackRatingStar < 25000) {
            return this.defaultClass + " yellow";
        }
        else if(feedbackRatingStar < 50000) {
            return this.defaultClass + " turquoise";
        }
        else if(feedbackRatingStar < 100000) {
            return this.defaultClass + " purple";
        }
        else if(feedbackRatingStar < 500000) {
            return this.defaultClass + " red";
        }
        else if(feedbackRatingStar < 1000000) {
            return this.defaultClass + " green";
        }
        else {
            return this.defaultClass + " silver";
        }

    }

}
