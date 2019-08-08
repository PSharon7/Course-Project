import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})

export class LocalServiceComponent {

    key: string = "wishList";
    value: any;

    constructor() {
        this.value = this.getValue();
    }

    saveValue(value : Object) {
        localStorage.setItem(this.key, JSON.stringify(value));
    }

    getValue() {
        let value = JSON.parse(localStorage.getItem(this.key));
        // console.log(value);
        return value === null ? [] : value;
    }

    isWishList(itemId: string) {

        let items = this.getValue();

        for(let i in items) {
            if (itemId === items[i].itemId) {
                return true;
            }
        }

        return false;
    }

    modifyWishList(itemId : string, item : any) {

        let items = this.getValue();

        if (this.isWishList(itemId)) {
            //remove
            for (let i in items) {
                if(items[i].itemId === itemId) {
                    items.splice(i, 1);
                }
            }
        }
        else {
            //add
            // console.log(item);
            items.push(item);
        }

        this.value = items;

        this.saveValue(items);
    }

}