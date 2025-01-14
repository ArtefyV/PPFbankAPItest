**Testovací aplikace PPF bank API**

Hlavním úkolem této aplikace je demonstrovat možnost získání reportu o transakcích vybraných podle čísla bankovního účtu prostřednictvím klientského requestu na REST API.

*Výstupní podmínky pro testování.*

1) V testovacím prostředí je nainstalován MS SQL server, na kterém je vytvořena prázdná databáze připravená k naplnění vzorovými informacemi.
2) Jsou k dispozici přihlašovací údaje pro uživatele databáze, který má plná práva k vytváření, aktualizaci, výběru a mazání tabulek.
3) Adresa URL serveru včetně čísla portu a názvu databáze, jakož i uživatelské jméno a heslo jsou v konfiguračním souboru config.json ve složce resources projektu. Tyto údaje by měly být pro testovací prostředí odpovídajícím způsobem upraveny.
4) Projekt je třeba importovat do Intellij IDEA a sestavit jako projekt Maven. Všechny požadované závislosti jsou uvedeny v souboru pom.xml a měly by být načteny během procesu sestavování.

*Testování aplikace.*

1) Po kompilaci projektu a spuštění aplikace se musíme ujistit, že aplikace reaguje. Za tímto účelem je vytvořen koncový bod GET /hi, který bude odpovídat na pozdravy.
2) Pro manipulaci s databází je vytvořen koncový bod POST /db. Pro testovací připojení k databázi musí být na tento bod odeslán request s tělem {"action" : "test"}.
3) Pokud je databáze prázdná, je nutné vytvořit strukturu tabulek. To se provádí prostřednictvím requestu POST /db s tělem {"action" : "setup"}.
4) Naplnění databáze ukázkovými informacemi se provádí prostřednictvím požadavku POST /db s tělem {"action" : "fill"}.
5) V případě potřeby lze databázi smazat prostřednictvím requestu POST /db s tělem {"action" : "drop"}, poté lze databázi znovu vytvořit a naplnit ji opakováním kroků 3. a 4.
6) Přehled o transakcích na účtu se provede prostřednictvím requestu GET /accounts/{accountId}/transactions, kde místo {accountId} je třeba nahradit číslo 2002222222, které je použito v informačním vzoru pro příklad.

*Další funkce.*

Aplikace také umožňuje vytvářet nové zápisy v tabulkách účtů, výpisů, typů transakcí a samotných transakcí. 
Koncové body vytvořené pro tento účel jsou:
1) pro vytváření účtů - POST /accounts/create
2) pro vytváření výpisů - POST /statements/create
3) pro vytváření typů transakcí - POST /transactions/type/create
4) pro vytváření transakcí - POST /transactions/create

Jako tělo requestu se používá JSON, kde jsou popsána všechna políčka potřebná k vytvoření příslušného zápisu. Ukázky těchto struktur lze převzít ze souborů accounts.json, statements.json, transactions.json, transactionTypes.json ve složce resources projektu a následně upravit hodnoty polí podle potřeby.

*Poznámky k implementaci projektu.*

Projekt je založen na frameworku Vert.X, aby se zjednodušilo vytváření serveru HTTP a konfigurace rozhraní API. Funkce Vert.X jsou zde však použity pouze v omezené míře. Metody práce s databází využívají v podstatě „tradiční“ prostředky knihovny java.sql a podobně.
Kód projektu obsahuje stručné popisy tříd a metod pro usnadnění analýzy kódu. Je také třeba vzít v úvahu, že tento projekt není prototypem žádného komerčního produktu, a proto je v něm řada řešení zjednodušena. Výsledkem je aplikace, která je schopna demonstrovat základní funkce, ale není připravena pro nasazení v reálném prostředí.
