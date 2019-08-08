import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { NgModule } from '@angular/core';

import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MatAutocompleteModule } from '@angular/material';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RoundProgressModule } from 'angular-svg-round-progressbar';
import { OrderModule } from 'ngx-order-pipe';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { ProductSearchComponent } from './product-search.component';
import { TableContainerComponent } from './table-container.component';
import { ResultTableComponent } from './result-table.component';
import { WishListTableComponent } from './wishlist-table.component';

import { DetailContainerComponent } from './detail-container.component';
import { InfoTabTableComponent } from './info-tab-table.component';
import { PhotoTabTableComponent } from './photo-tab-table.component';
import { ShippingTabTableComponent } from './shipping-tab-table.component';
import { SellerTabTableComponent } from './seller-tab-table.component';
import { SimilarTabTableComponent } from './similar-tab-table.component';

import { LoaderComponent } from './loader.component';
import { LoaderService } from './loader.service';
import { LoaderInterceptorService } from './loader-inspector.component';

@NgModule({
    declarations: [
        AppComponent,

        ProductSearchComponent,

        TableContainerComponent,
        ResultTableComponent,
        WishListTableComponent,

        DetailContainerComponent,
        InfoTabTableComponent,
        PhotoTabTableComponent,
        ShippingTabTableComponent,
        SellerTabTableComponent,
        SimilarTabTableComponent,

        LoaderComponent

    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ReactiveFormsModule,
        HttpClientModule,
        MatAutocompleteModule,
        BrowserAnimationsModule,
        MatTooltipModule,
        RoundProgressModule,
        OrderModule

    ],
    providers: [
        LoaderService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: LoaderInterceptorService,
            multi: true
        }
    ],
    bootstrap: [
        AppComponent
    ]
})

export class AppModule { }
