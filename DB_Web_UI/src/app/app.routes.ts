import { Routes } from '@angular/router';
import { Login } from './pages/auth/login/login';
import { Register } from './pages/auth/register/register';
import { Statistics } from './pages/statistics/statistics/statistics';
import { authGuard } from './core/auth.guard';
import { UserComponent } from './pages/users/user/user';
import { Favorites } from './pages/statistics/favorites/favorites';
import { Trips } from './pages/data/trips/trips';
import { Import } from './pages/import/import';
import { Disruptions } from './pages/data/disruptions/disruptions';
import { Prediction } from './pages/prediction/prediction';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },

  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: 'statistics', component: Statistics },
      { path: 'statistics/favorites', component: Favorites },
      { path: 'admin/trips', component: Trips },
      { path: 'admin/disruptions', component: Disruptions },
      { path: 'admin/import', component: Import},
      { path: 'profile', component: UserComponent },
      { path: 'prediction', component: Prediction },
      { path: '', redirectTo: 'statistics', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
