import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { WebServiceComponent } from './web-service.component';
import { TableContainerComponent } from './table-container.component';

@Component({
    providers: [TableContainerComponent],
    selector: 'product-search',
    templateUrl: './product-search.html',
    styleUrls: ['./product-search.css']
})

export class ProductSearchComponent implements OnInit {

    activeBtnClass = "btn btn-dark";
    inactiveBtnClass = "btn bg-white";

    resultBtnClass = this.activeBtnClass;
    listBtnClass = this.inactiveBtnClass;

    searchForm : FormGroup;
    zipcode : string = "";

    searchResult : any;

    constructor(private formBuilder: FormBuilder, public webService : WebServiceComponent, public tableContainer : TableContainerComponent) {
        // __await(this.getGeo());
    }

    async ngOnInit() {
        this.searchForm = this.formBuilder.group({
            keyword: ['', [
                Validators.required,
                this.noWhitespaceValidator
            ]],
            category: ['All Categories'],
            new: [false],
            used: [false],
            unspecified: [false],
            pickup: [false],
            shipping: [false],
            distance: [''],
            location: ['1'],
            zip: ['']
        }, {}
        );

        this.webService.getGeoLocation = false;
        this.setZipcodeValidators();

        await this.getGeo();

        if (window.screen.width < 500) { // 768px portrait
            this.webService.mobile = true;
        }

    }

    async onSubmit() {
        let zipValue = this.zipcode;
        if ( this.searchForm.get('location').value === '2') {
            zipValue = this.searchForm.get('zip').value;
        }

        this.webService.showResults = false;
        this.webService.searchResult = {};

        this.setResult();

        // console.log("Submit");
        await this.webService.getSearchResult(
            this.searchForm.get('keyword').value,
            this.searchForm.get('category').value,
            this.searchForm.get('new').value,
            this.searchForm.get('used').value,
            this.searchForm.get('unspecified').value,
            this.searchForm.get('pickup').value,
            this.searchForm.get('shipping').value,
            this.searchForm.get('distance').value,
            zipValue);

    }

    async Clear() {
        this.searchForm = this.formBuilder.group({
                keyword: ['', [
                    Validators.required,
                    this.noWhitespaceValidator
                ]],
                category: ['All Categories'],
                new: [''],
                used: [''],
                unspecified: [''],
                pickup: [''],
                shipping: [''],
                distance: [''],
                location: ['1'],
                zip: ['']
            }, {}
        );

        this.webService.getGeoLocation = false;
        this.setZipcodeValidators();
        await this.getGeo();

        this.setResult();

        this.webService.searchResult = {};
        this.webService.showResults = false;
        this.webService.previousShowTable = true;
        this.webService.showResultTable = this.webService.showWishlistTable = this.webService.showInfoTable = false;

        this.webService.getItemDetailId = '';
    }

    //
    async getGeo() {
        this.zipcode = await this.webService.getGeoLocationFunc();

        // if (this.zipcode) {
        //     this.webService.getGeoLocation = true;
        // }
    }

    // convenience getter for easy access to form fields
    get getValue() {
        return this.searchForm.controls;
    }

    /*
     *  Auto Complete
     */
    async OnZipChange(zip: string) {
        if (zip.trim() === '' || zip.indexOf(' ') >= 0) {
            this.webService.zipcodesAuto = [];
            console.log(this.webService.zipcodesAuto);
        }
        else {
            await this.webService.getZipCode(zip);
        }
        this.webService.zipcodesAutoing = true;
    }

    /*
     *  Input validation
     */

    //keyword whitespace
    private noWhitespaceValidator(control: FormControl) {
        const isWhitespace = (control.value || '').trim().length === 0;
        const isValid = !isWhitespace;
        return isValid ? null : { 'whitespace': true };
    }
    //
    // button change
    public setZipcodeValidators() {
        const zip = this.searchForm.get('zip');

        this.searchForm.get('location').valueChanges
            .subscribe(location => {

                if (location === '1') {
                    zip.setValidators(null);

                    this.webService.zipcodesAutoing = false;
                }

                if (location === '2') {
                    zip.setValidators([
                        Validators.required,
                        this.noWhitespaceValidator
                    ]);

                    this.webService.zipcodesAutoing = true;
                }

                zip.updateValueAndValidity();
            });
    }

    public zipcodeValidator(checked, value) {
        if (this.searchForm.get('location').value === '1' && !this.webService.getGeoLocation) {
            return true;
        }

        const control = new FormControl(value, [
            Validators.pattern('[0-9]{5}')
        ]);

        return control.errors;

    }

    setResult() {
        this.resultBtnClass = this.activeBtnClass;
        this.listBtnClass = this.inactiveBtnClass;
        this.webService.showResultTable = true;
        this.webService.showWishlistTable = this.webService.showInfoTable = false;
    }


    showResult() {
        this.resultBtnClass = this.activeBtnClass;
        this.listBtnClass = this.inactiveBtnClass;

        if (!this.webService.showResults) {
            this.webService.delayShowList();
        }
        else {
            this.webService.showResultTable = true;
            this.webService.showWishlistTable = this.webService.showInfoTable = false;
        }

        this.webService.previousShowTable = true;
    }


    showList() {
        this.resultBtnClass = this.inactiveBtnClass;
        this.listBtnClass = this.activeBtnClass;

        if (!this.webService.showResults) {
            this.webService.delayShowList();
        }
        else {
            this.webService.showWishlistTable = true;
            this.webService.showResultTable = this.webService.showInfoTable = false;
        }

        this.webService.previousShowTable = false;
    }

}
