import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';


@Component({
    selector: 'similar-tab-table',
    templateUrl: './similar-tab-table.html',
    styleUrls: ['./similar-tab-table.css']
})

export class SimilarTabTableComponent {

    orderBy: string = '';
    sortBy: string = 'Ascending';
    reverse: boolean = false;
    showLength: number = 5;

    btnValue = "Show More";
    btnValueMore = "Show More";
    btnValueLess = "Show Less";

    constructor(public webService : WebServiceComponent) {

    }

    changeOption(option: string) {
        if (option === 'default') {
            this.orderBy = '';
        }
        else {
            this.orderBy = option;
        }
    }

    changeOrder(option: string) {
        if (option != this.sortBy) {
            this.reverse = !this.reverse;
            this.sortBy = option;
        }
    }

    getSimilarItemNumber(cnt: number) {
        return cnt > 5;
    }

    changeBtnText() {
        if (this.btnValue === this.btnValueMore) {
            this.btnValue = this.btnValueLess;
            this.showLength = 20;
        }
        else {
            this.btnValue = this.btnValueMore;
            this.showLength = 5;
        }
    }


}
