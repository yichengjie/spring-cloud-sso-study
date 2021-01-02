import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'hello world';
    authenticated = false ;
    credentials ={username:"admin", password:"123456"} ;
    order ={id:"",productId:""} ;
    constructor(private http: HttpClient) {

    }
    login(){
        this.http.post('login', this.credentials).subscribe(()=>{
            this.authenticated = true ;
        }, ()=>{
            alert('auth fail !')
        }) ;
    }

    getOrder(){
        this.http.get("api/order/orders/1").subscribe(
            (data:any)=>{
                this.order = data;
            },()=>{
                alert("get order fail !")
            }
        ) ;
    }
}
