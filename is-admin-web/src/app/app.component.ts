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
                console.info("==========> 111111111111111111111")
            }
            if (!this.authenticated){
                console.info("==========> 222222222222222222222")
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
    logout(){
        this.http.get('logout').subscribe(()=>{
            // 在授权服务器上也退出登录
            //window.location.href = 'http://localhost:7777/logout' ;
            //this.authenticated = false ;
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
