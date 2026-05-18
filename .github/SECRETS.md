# CI/CD — GitHub Secrets

## Secrets obligatoires
| Secret | Description |
|--------|-------------|
| `BACKOFFICE_USER_EMAIL` | Email de connexion backoffice |
| `BACKOFFICE_USER_PASSWORD` | Mot de passe backoffice |

## Secrets optionnels
| Secret | Défaut si absent |
|--------|------------------|
| `BACKOFFICE_URL` | `https://stg-bo.noveocare.com/login` |
| `SELF_HEALING_API_URL` | désactivé (`self.healing.enabled=false`) |

## Exécution locale
Les credentials sont passés via `-D` :
```bash
mvn test -pl automation-framework -Dtest=LoginTestRunner \
  -Dbackoffice.user.email="mon@email.com" \
  -Dbackoffice.user.password="monpass"
```
