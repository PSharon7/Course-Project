import {animate, state, style, transition, trigger} from '@angular/animations';

export const SlideAnimation = trigger('slideAnimation', [

    transition('right => here', [
        style({ transform: 'translateX(150%)' }),
        animate('1s ease-in-out', style({ transform: 'translateX(0)' }))
    ]),
    transition('left => here', [
        style({ transform: 'translateX(-150%)' }),
        animate('1s ease-in-out', style({ transform: 'translateX(0)' }))
    ]),

    transition('* => here', [
        style({ transform: 'translateX(-150%)' }),
        animate('1s ease-in-out', style({ transform: 'translateX(0)' }))
    ]),

    transition('* => right', [
        style({ transform: 'translateX(0%)' }),
        animate('1s ease-in-out', style({ transform: 'translateX(150%)' }))
    ]),

    transition('* => left', [
        style({ transform: 'translateX(0%)' }),
        animate('1s ease-in-out', style({ transform: 'translateX(-150%)' }))
    ]),
]);