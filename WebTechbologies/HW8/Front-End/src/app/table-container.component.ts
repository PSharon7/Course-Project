import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';
import { LocalServiceComponent } from './local-service.component';
import { LoaderService } from './loader.service';
import { SlideAnimation } from './slide.animation';


@Component({
    selector: 'table-container',
    templateUrl: './table-container.html',
    styleUrls: ['./table-container.css'],
    animations: [
        SlideAnimation
    ]
})

export class TableContainerComponent {

    constructor(public webService : WebServiceComponent, public localService : LocalServiceComponent, public loader : LoaderService ) {
    }


}
