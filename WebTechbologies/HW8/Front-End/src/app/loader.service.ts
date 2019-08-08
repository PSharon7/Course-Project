import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { LoaderState } from './loader.model';

@Injectable({
    providedIn: 'root'
})

export class LoaderService {
    private loaderSubject = new Subject<LoaderState>();

    loaderState = this.loaderSubject.asObservable();

    constructor() { }

    showLoader: boolean = false;


    show() {
        this.showLoader = true;
        this.loaderSubject.next(<LoaderState>{ showLoader: true });
    }

    hide() {
        this.showLoader = false;
        this.loaderSubject.next(<LoaderState>{ showLoader: false });
        // setTimeout(()=>this.showLoader = false, 1500);
        // setTimeout(()=>this.loaderSubject.next(<LoaderState>{ showLoader: false }), 1500);

    }

}