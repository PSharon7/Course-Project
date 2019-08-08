import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { LoaderState } from './loader.model';
import { LoaderService } from './loader.service';

@Component({
    selector: 'loader',
    templateUrl: './loader.html',
    styleUrls: ['./loader.css']
})

export class LoaderComponent implements OnInit {

    showLoader = false;

    private subscription: Subscription;

    constructor(private loaderService: LoaderService) { }

    ngOnInit() {
        this.subscription = this.loaderService.loaderState
            .subscribe((state: LoaderState) => {
                this.showLoader = state.showLoader;
            });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}