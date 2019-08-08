import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';


@Component({
    selector: 'shipping-tab-table',
    templateUrl: './shipping-tab-table.html',
    styleUrls: ['./shipping-tab-table.css']
})

export class ShippingTabTableComponent {

    constructor(public webService : WebServiceComponent) {

    }

}
