import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';


@Component({
    selector: 'photo-tab-table',
    templateUrl: './photo-tab-table.html',
    styleUrls: ['./photo-tab-table.css']
})

export class PhotoTabTableComponent {

    constructor(public webService : WebServiceComponent) {

    }

}
