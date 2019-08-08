import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';


@Component({
    selector: 'info-tab-table',
    templateUrl: './info-tab-table.html',
    styleUrls: ['./info-tab-table.css']
})

export class InfoTabTableComponent {

    constructor(public webService : WebServiceComponent) {

    }

}
