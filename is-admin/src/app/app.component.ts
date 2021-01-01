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
    constructor(private http: HttpClient) {

    }
    login(){
        this.http.post('login', this.credentials).subscribe(()=>{
            this.authenticated = true ;
        }, ()=>{
            alert('auth fail !')
        }) ;
    }
}
