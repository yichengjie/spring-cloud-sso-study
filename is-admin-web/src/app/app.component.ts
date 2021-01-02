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
    credentials ={username:"admin", password:"admin"} ;
    order ={id:"",productId:""} ;
    constructor(private http: HttpClient) {
        this.http.get('me').subscribe((data:any)=>{
            if (data){
                this.authenticated = true ;
            }
            if (!this.authenticated){
                let url = 'http://localhost:7777/oauth/authorize?' ;
                url +=  'client_id=admin_service&'  ;
                url +=  'redirect_uri=http://localhost:8280/oauth/callback&' ;
                url += 'response_type=code&' ;
                url += 'state=123' ;
                window.location.href = url ;
            }
        },()=>{
            alert('get me fail !')
        }) ;
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
