import { Component } from '@angular/core';
import { WebServiceComponent } from './web-service.component';
import { LocalServiceComponent } from './local-service.component';

@Component({
    selector: 'wishlist-table',
    templateUrl: './wishlist-table.html',
    styleUrls: ['./wishlist-table.css']
})

export class WishListTableComponent {

    constructor(public webService : WebServiceComponent, public localService : LocalServiceComponent) {
    }

    async itemDetail(itemId : string, item: any) {
        console.log(itemId);
        await this.webService.getItemDetail(itemId, item);
    }

    getTotalShopping() {
        let items = this.localService.getValue();
        let totalPrice = 0;
        for(let i in items) {
            totalPrice += Number(items[i].price.slice(1));
        }

        return totalPrice;
    }

}
