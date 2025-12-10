import {inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

export interface UserFormDTO{
    firstName: string;
    lastName: string;
    email: string;
    password: string;
}

export interface UserResponseDTO{
    id: number;
    name: string;
    email: string;
}

@Injectable({
    providedIn: 'root'
})
export class AuthenticationService{

    private router = inject(Router)
    private http = inject(HttpClient);
    private apiURL = 'http://localhost:8080'

    user = signal<UserResponseDTO | null>(null);

    logIn(userForm: UserFormDTO){
        this.http.post(`${this.apiURL}/api/users/login`, userForm).subscribe({
            next: (response: any) => {
                this.user.set(response);
                this.router.navigateByUrl('/mail');
            },
            error: (e) => {
                if (e.error && e.error.error) {
                    console.log(`Error: ${e.error.error}`);
                } else {
                    console.log('Unknown error', e);
                }
            }

        })
    }

    signUp(userForm: UserFormDTO){
        this.http.post(`${this.apiURL}/api/users/register`, userForm).subscribe({
            next: (response: any) => {
                this.user.set(response);
                this.router.navigateByUrl('/mail');
            },
            error: (e) => {
                if (e.error && e.error.error) {
                    console.log(`Error: ${e.error.error}`);
                } else {
                    console.log('Unknown error', e);
                }
            }
        })
    }

    signOut(){
        this.user.set(null);
        this.router.navigateByUrl('/login');
    }
}