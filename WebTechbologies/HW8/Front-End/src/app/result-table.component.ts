import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';
import { LocalServiceComponent } from './local-service.component';
import {LoaderService} from './loader.service';

@Component({
    selector: 'result-table',
    templateUrl: './result-table.html',
    styleUrls: ['./result-table.css']
})

export class ResultTableComponent {

    activePageBtnClass = "btn btn-dark-page";
    inactivePageBtnClass = "btn btn-page";

    constructor(public webService : WebServiceComponent, public localService : LocalServiceComponent, public loader : LoaderService) {
    }

    showPage(pageNumber: number) {
        this.webService.currentPage = pageNumber;
    }

    showPrevPage() {
        this.webService.currentPage--;
    }

    showNextPage() {
        this.webService.currentPage++;
    }

    async itemDetail(itemId : string, item : any) {

        await this.webService.getItemDetail(itemId, item);
    }

}
