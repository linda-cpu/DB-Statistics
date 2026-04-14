import { Component, inject } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Menubar } from 'primeng/menubar';
import { AuthService } from '../../core/auth-service';
import { RouterLink } from '@angular/router';
import { Avatar } from 'primeng/avatar';

@Component({
  selector: 'app-navbar',
  imports: [Menubar, RouterLink, Avatar],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  authService = inject(AuthService);
  items: MenuItem[] = [];

  ngOnInit() {
    this.items = [
      //User Sections
      {
        label: 'Statistiken',
        icon: 'pi pi-chart-bar',
        routerLink: '/statistics',
        visible: !this.authService.isAdmin(),
      },
      {
        label: 'Favoriten',
        icon: 'pi pi-heart',
        routerLink: 'statistics/favorites',
        visible: !this.authService.isAdmin(),
      },

      //Admin Sections
      {
        label: 'Analysen',
        icon: 'pi pi-chart-line',
        items: [
          { label: 'Statistiken', icon: 'pi pi-chart-bar', routerLink: '/statistics' },
          { label: 'Favoriten', icon: 'pi pi-heart', routerLink: 'statistics/favorites' },
        ],
        visible: this.authService.isAdmin(),
      },
      {
        label: 'Trips einsehen',
        icon: 'pi pi-map',
        routerLink: '/admin/trips',
        visible: this.authService.isAdmin(),
      },
      {
        label: 'Import',
        icon: 'pi pi-file-import',
        routerLink: '/admin/import',
        visible: this.authService.isAdmin(),
      },
    ];
  }
}
