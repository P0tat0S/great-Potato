/**************************
   Author: Mark Fernandez
   Date: 01/10/19
**************************/

//Import utilities
import java.util.*;
import java.io.*;

class trappedCastle {
   public static void main(String[] args) //MAIN method
   {
      //Player creation
      Player p = playerCreation();
      playerPrinter(p);
      print("This is your Status Card, to view it, you can type S anywhere.\n");

      //Welcome message
      print(randomEntrance() + "\n\nWalking down the entrance of the castle" +
         " you find yourself in the Great Hall. Looking around you don't find" +
         " anything of value or interest with the exception of three weird" +
         " looking exits.");

      //Initialise rooms
      int rC = 0;
      Room[] rooms = new Room[300]; //Max of 100 rooms traversed.

      //Main premise while alive
      while (getHealth(p) > 0) {
         //Room generator, rC stands for roomCounter
         rooms = generateRoom(rC, rooms);

         //Enter one of the three Rooms
         p = roomEvent(p, rooms, rC);

         //Generate next three rooms
         rC += 3;

         //Check if dead and exit if true
         if (getHealth(p) <= 0) {
            print("\nYour health has reached " + getHealth(p) + "\n\n" +
               randomDeath() + "\n\nFinal Score:");
            playerPrinter(p);
            print("\nGAME OVER");
            System.exit(0);
         }

         //Check if level Up
         if (getXp(p) >= getXpMax(p)) {
            print("Congratulations you just leveled up");
            p.level++;
            //Upgrade player's stats
            p = setStats(p);
            //Any extra xp is kept for next level
            int extraXP = getXp(p) % getXpMax(p);
            p.xpMax *= 1.2;
            p.xp = extraXP;
            playerPrinter(p);
         }

      } //End main while loop
   } //End main

   /********************************************
   Methods to send the player into random events
   ********************************************/
   public static Player roomEvent(Player p, Room[] rooms, int rC) {
      //Before entering check if the player is above max health
      if (getHealth(p) > getMaxHealth(p)) p.health = getMaxHealth(p);
      entranceInfo(rooms, rC); //Room information printer
      print("Rooms generated " + (rooms[rC + 2].roomNumber));
      String choice = playerChoice(p);
      if (choice.equalsIgnoreCase("L")) p = enterRoom(p, rooms[rC]);
      else if (choice.equalsIgnoreCase("F")) p = enterRoom(p, rooms[rC + 1]);
      else if (choice.equalsIgnoreCase("R")) p = enterRoom(p, rooms[rC + 2]);
      return p;
   } //End roomEvent

   public static Player enterRoom(Player p, Room room) {
      //Method that sends player into different room types
      print("\nYou have entered " + getRoomName(room));
      if (getRoomType(room).equals("Boss"))
         enterBoss(p);
      else if (getRoomType(room).equals("Combat"))
         enterCombat(p);
      else if (getRoomType(room).equals("Relic"))
         enterRelic(p);
      else if (getRoomType(room).equals("Health"))
         enterHealth(p);
      return p;
   }

   //Enter one of the different types of rooms
   public static Player enterBoss(Player p) {
      //Method for an monster encounte
      print("\nYou encounter a really though enemy. This wont be easy.\n");
      int difficulty = diceRoller(4, 6); //Random enemy stats
      p = combatPhase(p, difficulty); //Send player to a Combat
      p.relics++; //Rewards
      print("The enemy dropped a relic!");
      int d2 = diceRoller(1,2);
      if (d1==1 && weaponChance(getLuck(p))>=95)  {
         Weapon [] weapons = generateWeapon(getLuck(p),getLevel(p));
      }  else if (d==2 && armorChance(getLuck(p)>=95))  {
         Armor [] armors = generateArmor(getLuck(p),getLevel(p));
      }
      roomSeparator(); //Prints a line of "-" to cover a room event
      return p; //Returns player after changes
   }

   public static Player enterCombat(Player p) {
      //Same method as enterBoss with less rewards and less difficult
      print("\nYou encounter an enemy. You will not continue unscathed.\n");
      int difficulty = diceRoller(1, 3);
      p = combatPhase(p, difficulty);
      roomSeparator();
      return p;
   }

   public static Player enterRelic(Player p) {
      print("\nIn the middle of the room you see old relics.");
      String trap = randomTrap(); //Random trap descriptions
      while (true) {
         String decision = stringInput(
            "Do you want to pick them up? (Y)es/(N)o");
         if (decision.equalsIgnoreCase("Y")) {
            p.relics += 2;
            int d4 = diceRoller(1, 4);
            if (d4 == 4) {
               print("You manage to grab the relics. But");
               print(trap);
               int damage = diceRoller(1, 3) * getLevel(p);
               p.health -= damage;
               print("You receive " + damage + " points of damage.");
            } else {
               print("You grab the relics successfully and find a few coins" +
                  " lying around.");
               p.money += diceRoller(1, 2) * getLevel(p);
            }
            break;
         } else if (decision.equalsIgnoreCase("N"))   {
            print("You refuse to grab the relics and continue you journey");
            break;
         } else if (decision.equalsIgnoreCase("S")) playerPrinter(p);
         else print("That was not a decision. Try again...");
      } //End while loop
      roomSeparator();
      return p; //Method to get relics(score)
   }

   public static Player enterHealth(Player p) {
      print("\nA room with a fountain in the middle, you rest.");
      int heal = (int)(diceRoller(1, 4) * (getLevel(p) /
      2.0)); //Heal randomly
      p.health += heal;
      print("You have healed for " + heal);
      roomSeparator();
      return p; //Method that heals the player
   }

   /*************
   Combat Methods
   *************/
   public static Player combatPhase(Player p, int difficulty) {
      int dangerLevel = difficulty * getLevel(p);
      Enemy e = generateEnemy(dangerLevel); //Enemy Creation
      int enemyStats = getEnemyAttack(e)+getEnemyDefense(e)+getEnemyHealth(e);
      //Temporary buffs
      boolean defended = false;
      boolean buffed = false;
      while (true) //Will break when either is defeated or when running
      {
         combatInfo(p, e); //At start of combat print the state of the combat
         if (getEnemyHealth(e) <= 0) {//If enemy is defeated
            int gainedMoney = (int)(diceRoller(1, 3) * enemyStats / 10.0);
            print("\nThe combat has ended.");
            print("You have gained " + enemyStats + " Experience Points!");
            p.xp += enemyStats;
            print("The enemy dropped " + gainedMoney + " coins!");
            p.money += gainedMoney;
            break; //Stop and get rewards
         }
         //If player is dead
         else if (getHealth(p) <= 0) {
            print("You have lost the combat");
            break; //Stop and GAME OVER
         } else /**COMBAT PROCEDURES**/ {
            /************
            Player's Turn
            ************/
            if (defended) //Return defense to normal at start
            {
               p.defense /= 2;
               defended = false;
            }
            String action = playerTurn(p); //Get player input
            if (action.equalsIgnoreCase("A")) //Attack the enemy
            {
               int damageDealt = damageCalculationP(p, e);
               e.health -= damageDealt;
            } else if (action.equalsIgnoreCase("D")) //Defend the enemy attack
            {
               print("You entered a defensive position");
               p.defense *= 2;
               defended = true;
            } else if (action.equalsIgnoreCase("M")) //Cast a spell
            {
               p = castSpell(p, e);
               e.health -= getMagicDamage(p);
            } else if (action.equalsIgnoreCase("R")) //Run aways
            {
               int runDamage = (int)(diceRoller(1, 3) * enemyStats / 10.0);
               p.health -= runDamage;
               print("\nYou successfully run away but receive " + runDamage +
                  " points of damage running away.");
               break; //Stop and return damaged
            } //End Player's Turn

            /***********
            Enemy's Turn
            ***********/
            int d10 = diceRoller(1, 10);
            //Enemy actions is either attack or enrage to deal double damage
            if (d10 <= 2 && !buffed && !(getEnemyHealth(e) <=0)) //If enraged
            {
               print("The enemy enrages");
               buffed = true;
            } else if (!(getEnemyHealth(e) <=0)) //Prevent dead enemy action
            {
               if (buffed) //If enraged
               {
                  int enemyDamage = damageCalculationE(p, e, buffed);
                  p.health -= enemyDamage;
                  buffed = false;
               } else {
                  int enemyDamage = damageCalculationE(p, e, buffed);
                  p.health -= enemyDamage;
               }
            } //End Enemy's Turn
         } //End Combat Mechanics
      }
      return p;
   } //End Combat Phase

   public static int damageCalculationP(Player p, Enemy e) {
      //Damage calculation of the player when attacking
      int d100 = diceRoller(1, 100);
      int critical = (int)(0.80 * getLuck(p) + 0.20 * getDexterity(
      p)); //Crit chance
      print("Critical roll:" + d100 + " /" + critical);
      boolean crit = false;
      if (d100 <= critical) //Check if it is a critical attack
      {
         p.attack *= 2; //Double attack
         crit = true;
      }
      int damage = (getAttack(p) - getEnemyDefense(
      e)); //Simple damage formula
      if (damage <= 1) damage = diceRoller(1, getLevel(
      p)); //Random damage if <=1
      if (crit) {
         print("CRITICAL HIT!");
         p.attack /= 2;
         crit = false; //Return attack to normal
      }
      print("You attack for " + damage);
      return damage; //Return the damage that the player will deal
   }

   public static int damageCalculationE(Player p, Enemy e, boolean buffed) {
      if (buffed) e.attack *= 2; //Double attack if enraged
      int damage = (getEnemyAttack(e) - getDefense(p));
      if (damage <= 1) damage = diceRoller(1, getLevel(p));
      int d100 = diceRoller(1, 100);
      int evade = (int)(0.80 * getDexterity(p) + 0.20 * getLuck(
      p)); //Evade chance
      print("Evade roll:" + d100 + " /" + evade);
      if (d100 <= evade) //Check if you evaded the attack
      {
         damage = 0;
         print("You evade the attack!");
      }
      if (buffed) e.attack /= 2;
      print("The " + getEnemyType(e) + " attacks and deals " + damage);
      return damage; //Return the damage dealt by the enemy
   }

   public static Player castSpell(Player p, Enemy e) {
      Magic[] m = generateSpells();//Creation of a set of spells
      String choice = "";
      while (true) {
         choice = stringInput(printSpells(p, m)
         +"\nTo cast a Spell, type the corresponding spell number: ");
         if (choice.equals("1")) {//Send Player, Enemy and the Spell into calc.
            p.magicDamage = magicDamage(p, e, m[0]);//Store magic damage dealt
            p.mana -= getManaCost(m[0]);//Substract the mana used
            break;
         //Check if Player has enough mana and level to cast the Spell
         } else if (choice.equals("2") && getLevel(p) >= getUnlockLevel(m[1])
         && getMana(p) >= getManaCost(m[1])) {
            p.magicDamage = magicDamage(p, e, m[1]);
            p.mana -= getManaCost(m[1]);
            break;
         } else if (choice.equals("3") && getLevel(p) >= getUnlockLevel(m[2])
         && getMana(p) >= getManaCost(m[2])) {
            p.magicDamage = magicDamage(p, e, m[2]);
            p.mana -= getManaCost(m[2]);
            break;
         } else if (choice.equals("4") && getLevel(p) >= getUnlockLevel(m[3])
         && getMana(p) >= getManaCost(m[3])) {
            p.magicDamage = magicDamage(p, e, m[3]);
            p.mana -= getManaCost(m[3]);
            break;
         } else if (choice.equals("5") && getLevel(p) >= getUnlockLevel(m[4])
         && getMana(p) >= getManaCost(m[4])) {
            p.magicDamage = magicDamage(p, e, m[4]);
            p.mana -= getManaCost(m[4]);
            break;
         } else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else print("You cannot cast that. Please try again...");
      }
      return p;//Return the magic damage dealth within the Player
   }//End castSpell

   public static int magicDamage(Player p, Enemy e, Magic m) {
      int totalDamage = (int)(getDmgMultiplier(m) * getAttack(p) - 0.5 *
         getEnemyDefense(e));//Formula for magic damage
      print("You cast " + getSpellName(m) + "!" + "\nYou deal " +
         totalDamage + " damage!");
      return totalDamage;//Return the damaage dealt
   }//End magicDamage


   /****************************************
   Methods that get a choice from the player
   ****************************************/
   public static String playerChoice(Player p) {
      String choice;
      while (true) //Prevent non-wanted letters
      {
         choice = stringInput(
            "\nType a letter to go: (L)eft, (F)orward, (R)ight");
         if (choice.equalsIgnoreCase("L")) break;
         else if (choice.equalsIgnoreCase("F")) break;
         else if (choice.equalsIgnoreCase("R")) break;
         else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else print("\nThat was not one of the paths. Please try again.");
      }
      return choice; //Returns the player's choice
   }

   public static String playerTurn(Player p) {
      String choice;
      while (true) //Prevent other letters
      {
         choice = stringInput(
            "\nType a letter to perform an action: (A)ttack," +
            " (M)agic, (D)efend, (R)un");
         if (choice.equalsIgnoreCase("A")) break;
         else if (choice.equalsIgnoreCase("D")) break;
         else if (choice.equalsIgnoreCase("R")) break;
         else if (choice.equalsIgnoreCase("M") && getMana(p) >= 4) break;
         else if (choice.equalsIgnoreCase("M")) print("Not enough mana.");
         else if (choice.equalsIgnoreCase("S")) playerPrinter(p);
         else print("\nThat was not an action. Please try again.");
      }
      return choice; //Returns a letter
   }

   /***************************
   Methods to generate an Enemy
   ***************************/
   public static Enemy generateEnemy(int dangerLevel) {
      Enemy e = new Enemy();
      e.level = dangerLevel; //Set enemy level to its danger level
      double factor = getEnemyLevel(e) / 10.0;
      e.name = randomEnemyName(); //~Sets the name randomly
      e.type = randomEnemyType(); //~Sets the type randomly
      //Each type of enemy has its random range of stats
      if (getEnemyType(e).equals("Zombie")) {
         e.health = (int)((1 + factor) * diceRoller(8, 13));
         e.attack = (int)((1 + factor) * diceRoller(4, 6));
         e.defense = (int)((1 + factor) * diceRoller(5, 8));
      } else if (getEnemyType(e).equals("Goblin")) {
         e.health = (int)((1 + factor) * diceRoller(5, 8));
         e.attack = (int)((1 + factor) * diceRoller(9, 15));
         e.defense = (int)((1 + factor) * diceRoller(2, 4));
      } else if (getEnemyType(e).equals("Slime")) {
         e.health = (int)((1 + factor) * diceRoller(6, 10));
         e.attack = (int)((1 + factor) * diceRoller(2, 4));
         e.defense = (int)((1 + factor) * diceRoller(8, 13));
      } else if (getEnemyType(e).equals("Skeleton")) {
         e.health = (int)((1 + factor) * diceRoller(5, 9));
         e.attack = (int)((1 + factor) * diceRoller(5, 9));
         e.defense = (int)((1 + factor) * diceRoller(5, 9));
      } else if (getEnemyType(e).equals("Dark Knight")) {
         e.health = (int)((1 + factor) * diceRoller(8, 13));
         e.attack = (int)((1 + factor) * diceRoller(9, 15));
         e.defense = (int)((1 + factor) * diceRoller(8, 13));
      }
      return e; //Returns created enemy
   }

   public static String randomEnemyType() {
      int d5 = diceRoller(0, 4);
      //Health, Attack, Defense, Average, High
      String[] enemyType = {
         "Zombie", "Goblin", "Slime", "Skeleton", "Dark Knight" };
      return enemyType[d5]; //Returns a random enemy type
   } //End randomEnemyType

   public static String randomEnemyName() {
      int d40 = diceRoller(0, 39);
      String[] enemyName = {"Moransab","Mavorgezu","Thargha","Zergta","Vresan",
      "Thild'ula","Ha","Fenu","Imilphu","Bucu","Ronba","Nexla","Doomimgash",
      "Ball","Xyal","Grorn","Raruk","Mali","Thanxus","Irath","Rend","Hevorg",
      "Kil'grorn","Thusra","Bachom","Rornushang","Rothme","Dresh",
      "Rothshandze","Reshra","Naush","Motar","Baalshu","Lavi","Phekahud",
      "Zargver","Rath","Varorsharg","Kukruul","Becain"};
      return enemyName[d40]; //Returns a random enemy name
   } //End randomEnemyName

   /***********************
   Methods to create a room
   ***********************/
   public static Room[] generateRoom(int rC, Room[] rooms) {
      int roomsToCreate = rC + 3;
      while (rC < roomsToCreate) //while loop that will create the rooms
      {
         rooms[rC] = new Room();
         rooms[rC].roomNumber = rC;
         rooms[rC].roomType = randomRoomType(); //~gets room type
         rooms[rC].roomName = randomRoomName(); //~gets room name
         if (getRoomType(rooms[rC]).equals("Boss")) {
            rooms[rC].dangerLevel = 6; //~sets danger level
         } else if (getRoomType(rooms[rC]).equals("Combat")) {
            rooms[rC].dangerLevel = diceRoller(4, 5);
         } else if (getRoomType(rooms[rC]).equals("Relic")) {
            rooms[rC].dangerLevel = diceRoller(2, 3);
         } else if (getRoomType(rooms[rC]).equals("Health")) {
            rooms[rC].dangerLevel = 1;
         }
         rC++; //Increase the counter and create a new room
      } //End while loop to create rooms
      return rooms;
   } //End generateRoom

   public static String randomRoomType() {
      int d6 = diceRoller(1, 6);
      String type;
      if (d6 == 6) type = "Boss";
      else if (d6 == 4 || d6 == 5 || d6 == 3) type = "Combat";
      else if (d6 == 2) type = "Relic";
      else type = "Health";
      return type; //Method that returns a random type that will define the room
   } //End randomRoomType

   public static String randomRoomName() {
      String roomName = ("The " + randomAdjective() + " " + randomNoun() +
         " room.");
      return roomName; //Method that calls random generators and sets the name
   } //End randomRoomName;

   /********************************************
   Methods to initialise the Player and level Up
   ********************************************/
   public static Player playerCreation() {
      Player p = new Player();
      p.name = stringInput(
      "WELCOME\nWhat is your name?"); //get player's name
      p.job = jobSelection(); //~makes them choose a job
      p = setStats(p); //Set combat stats
      p.xp = 0;
      p.xpMax = 100;
      p.level = 1;
      p.money = 0;
      p.relics = 0;
      return p; //Set everything else to a default state
   } //End playerCreation

   public static String jobSelection() {
      //Method that gives the description of each job and makes them choose one
      String job = "none";
      print("\nChoose a starting job:");
      print("\nWARRIOR: It has the highest HEALTH stat and great ATTACK and " +
         "DEFENSE stats but lacks MANA.");
      print("\nMAGE: It has the highest MANA stat followed by a high ATTACK stat" +
         " stats but lacks HEALTH.");
      print("\nROGUE: It has the highest ATTACK stat followed by a high DEXTERITY" +
         " stat but lacks HEALTH and MANA.");
      print("\nJESTER: Jack of all trades but master of none, perfectly balanced" +
         " stats throughout.");
      print("\nVILLAGER: Weakest character with really low stats" +
         " but with really tremendous LUCK.");
      while (true) //while loop to prevent "other" jobs
      {
         job = stringInput("\nType the job you want to get");
         if (job.equalsIgnoreCase("WARRIOR")) break;
         else if (job.equalsIgnoreCase("MAGE")) break;
         else if (job.equalsIgnoreCase("ROGUE")) break;
         else if (job.equalsIgnoreCase("JESTER")) break;
         else if (job.equalsIgnoreCase("VILLAGER")) break;
         else print("\nThat is not a job, please try again.");
      }
      return job;
   } //End jobSelection

   public static Player setStats(Player p) {
      //Method that will set the player attributes
      double factor = getLevel(p) / 10.0;
      if (getJob(p).equalsIgnoreCase("WARRIOR")) {
         p.health = (int)((1 + factor) * 20);
         p.maxHealth = (int)((1 + factor) * 20);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 16);
         p.defense = (int)((1 + factor) * 16);
         p.dexterity = (int)((1 + factor) * 12);
         p.luck = (int)((1 + factor) * 12);
      } else if (getJob(p).equalsIgnoreCase("MAGE")) {
         p.health = (int)((1 + factor) * 10);
         p.maxHealth = (int)((1 + factor) * 10);
         p.mana = (int)((1 + factor) * 20);
         p.maxMana = (int)((1 + factor) * 20);
         p.attack = (int)((1 + factor) * 18);
         p.defense = (int)((1 + factor) * 12);
         p.dexterity = (int)((1 + factor) * 10);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("ROGUE")) {
         p.health = (int)((1 + factor) * 12);
         p.maxHealth = (int)((1 + factor) * 12);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 20);
         p.defense = (int)((1 + factor) * 12);
         p.dexterity = (int)((1 + factor) * 18);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("JESTER")) {
         p.health = (int)((1 + factor) * 14);
         p.maxHealth = (int)((1 + factor) * 14);
         p.mana = (int)((1 + factor) * 14);
         p.maxMana = (int)((1 + factor) * 14);
         p.attack = (int)((1 + factor) * 14);
         p.defense = (int)((1 + factor) * 14);
         p.dexterity = (int)((1 + factor) * 14);
         p.luck = (int)((1 + factor) * 14);
      } else if (getJob(p).equalsIgnoreCase("VILLAGER")) {
         p.health = (int)((1 + factor) * 8);
         p.maxHealth = (int)((1 + factor) * 8);
         p.mana = (int)((1 + factor) * 8);
         p.maxMana = (int)((1 + factor) * 8);
         p.attack = (int)((1 + factor) * 8);
         p.defense = (int)((1 + factor) * 8);
         p.dexterity = (int)((1 + factor) * 8);
         p.luck = (int)((1 + factor) * 44);
      }
      return p;
   } //End setStats

   /***********************
   Method to create spells
   ***********************/
   public static Magic[] generateSpells() {
      Magic[] m = new Magic[5];
      String[] spellNames = {
         "Magic Missile", "Frost Bolt", "Fireball",
         "Thundercrack", "Void Blast" };
      int[] unlockLevels = { 1, 3, 6, 9, 12 };
      int[] manaCosts = { 4, 8, 12, 16, 20 };
      double[] dmgMultipliers = { 0.75, 1.00, 1.25, 1.50, 2.00 };
      for (int i = 0; i < m.length; i++) {
         m[i] = new Magic();
         m[i].spellName = spellNames[i];
         m[i].unlockLevel = unlockLevels[i];
         m[i].manaCost = manaCosts[i];
         m[i].dmgMultiplier = dmgMultipliers[i];
      }
      return m;
   }

   /**************
   Utility methods
   **************/
   public static void print(String message) {
      System.out.println(message);
      return; //Method that abbreviates the println method
   } //End print

   public static String stringInput(String message) {
      print(message);
      Scanner sc = new Scanner(System.in); //Creation of a scanner
      String input = sc.nextLine();
      return input; //Method that returns the user's string input
   } //End stringInput

   public static int diceRoller(int min, int max) {
      Random random = new Random(); //Creation of an object Random
      //Gets two integers and returns a random value between the given values
      int dice = random.nextInt(max + 1 - min) + min;
      return dice;
   } //End diceRoller

   /**********************
   User Interface printers
   **********************/
   public static void playerPrinter(Player p) {
      print("\n******************************************");
      print("       Name:" + getName(p) + "      Job:" + getJob(p));
      print("       XP:" + getXp(p) + "/" + getXpMax(p) + "      Level:"
           +getLevel(p));
      print("       Money:" + getMoney(p) + "        Relics:" + getRelics(p));
      print("       Health:" + getHealth(p) + "/" + getMaxHealth(p)
           +"   Mana:" + getMana(p) + "/" + getMaxMana(p));
      print("       Attack:" + getAttack(p) + "      Defense:" + getDefense(p));
      print("       Dexterity:" + getDexterity(p) + "   Luck:" + getLuck(p));
      print("******************************************");
      return; //Status card printer of the Player
   } //End playerPrinter

   public static void roomSeparator() {
      print("----------------------------------------------------------------"
       + "-----------------------------------------------");
      return; //Method that just prints -'s
   }

   public static void entranceInfo(Room[] rooms, int rC) {
      String door = randomDoor();
      print("\nBefore you the three " + door + " stand. Small signs above, " +
         "give you the name of the rooms and their danger level:" +
         "\n\n" + "(L)" + getRoomName(rooms[rC]) +
         "\nDanger Level:" + diceRoller(1, 3)*getDangerLevel(rooms[rC]) +
         "\n\n" + "(F)" + getRoomName(rooms[rC + 1]) +
         "\nDanger Level:" + diceRoller(1, 3) * getDangerLevel(rooms[rC + 1]) +
         "\n\n" + "(R)" + getRoomName(rooms[rC + 2]) +
         "\nDanger Level:" + diceRoller(1, 3) * getDangerLevel(rooms[rC +2]));
      return; //Prints the information of all tree rooms in the intersection
   }

   public static void combatInfo(Player p, Enemy e) {
      try {
         if (getEnemyType(e).equals("Zombie")) {
            File file = new File(
               "C:\\Users\\mark_\\OneDrive\\Documentos\\Java\\miniProject\\sprites\\zombie.txt");//Read file
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line; //Store text into a single String
            while ((line = bR.readLine()) !=null) //Read all the lines
               print(line); //Print the whole file
         } else if (getEnemyType(e).equals("Goblin")) {
            File file = new File(
               "C:\\Users\\mark_\\OneDrive\\Documentos\\Java\\miniProject\\sprites\\goblin.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Slime")) {
            File file = new File(
               "C:\\Users\\mark_\\OneDrive\\Documentos\\Java\\miniProject\\sprites\\slime.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Skeleton")) {
            File file = new File(
               "C:\\Users\\mark_\\OneDrive\\Documentos\\Java\\miniProject\\sprites\\skeleton.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         } else if (getEnemyType(e).equals("Dark Knight")) {
            File file = new File(
               "C:\\Users\\mark_\\OneDrive\\Documentos\\Java\\miniProject\\sprites\\dark_Knight.txt");
            BufferedReader bR = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bR.readLine()) != null)
               print(line);
         }
      } catch (Exception exception) {
         exception.printStackTrace();
      }
      print(getEnemyName(e) + " the " + getEnemyType(e));
      print("Health:" + getEnemyHealth(e) + " Attack:" + getEnemyAttack(e) +
         " Defense:" + getEnemyDefense(e));
      print("\n" + getName(p) + " the " + getJob(p));
      print("Health:" + getHealth(p) + " Mana:" + getMana(p) + " Attack:" +
         getAttack(p) + " Defense:" + getDefense(p));
      return;
   }

   public static String printSpells(Player p, Magic[] m) {
      print("\nAll spells ignore half of the enemy's defenses.");
      String spells = "";
      int spellsPrinted = 0;
      for (int i = 0; i < 5; i++) { //Concatenate spell information.
         spells += "\n\nSpell " + "(" + (i + 1) + ")" + ": " +
            getSpellName(m[i]) + "\nMana Cost: " + getManaCost(m[i]) +
            "\nDamage multiplier: " + getDmgMultiplier(m[i]);
         spellsPrinted++; //Counter to print next spells after checking level
         if (getLevel(p) < getUnlockLevel(m[1]))
            break;
         else if (getLevel(p) < getUnlockLevel(m[2]) && spellsPrinted == 2)
            break;
         else if (getLevel(p) < getUnlockLevel(m[3]) && spellsPrinted == 3)
            break;
         else if (getLevel(p) < getUnlockLevel(m[4]) && spellsPrinted == 4)
            break;
      }
      return spells;
   }

   /*********************************
   Methods to get random descriptions
   *********************************/
   public static String randomEntrance() {
      //Random number between 1 and 3
      int d3 = diceRoller(1, 3);
      String message;
      if (d3 == 1) {
         message =
            "A regular gate with wide metal doors, a regular bridge and" +
            " a moat guards the only place with water within these hot," +
            " dry lands and it's the only way in.";
      } else if (d3 == 2) {
         message =
            "A great gate with thick wooden doors and hot oil pots guards" +
            " the only passage into the castle build upon a mountain top" +
            " and it's the only easy way in.";
      } else {
         message =
            "A vast gate with enormous metal doors, a regular bridge and" +
            " hot oil pots guards the only entrance to the castle build" +
            " at the edges of a shoreline and it's the only way in.";
      }
      return message; //returns one of the tree entrance messages
   } //End randomEntrance

   public static String randomDeath() {
      //Random number between 1 and 3
      int d3 = diceRoller(1, 3);
      String message;
      if (d3 == 1) {
         message =
            "I can't take this much longer. My entire body screams, " +
            "telling me to lie down and sleep. I must resist. If I sleep" +
            " I'll surely die, but I can't take this much longer. I'll" +
            " just.. I'll just lie down for a little while. Save my" +
            " energy, I'll make it out of this mess soon enough.";
      } else if (d3 == 2) {
         message =
            "Oh god, I can see it now. This is my end. My body is broken" +
            " beyond repair, I grow weaker by the minute and nobody is" +
            " going to find me. It would be too late even if they did." +
            " I'm going to die. That's okay, I give up.";
      } else {
         message =
            "I can't move, I can't think, I can't do anything. This can" +
            " only mean the end for me, I'm beyond the point of no return" +
            ". I'm going to die.. I'm going to die! No, no, no, no, no!" +
            " Please, I don't want to die.";
      }
      return message;
   } //End randomDeath

   public static String randomAdjective() {
      int d50 = diceRoller(0, 49);
      String[] adjectives = {"scintillating","acceptable","barbarous","flashy",
      "noxious","laughable","normal","hurried","puzzled","oval","overrated",
      "scandalous","dark","bloody","adventurous","lethal","groovy","whole",
      "adaptable","alive","smoltering","different","lopsided","malicious",
      "troubled","knowledgeable","simplistic","brash","encouraging","dusty",
      "inexpensive","smart","incandescent","weary","workable","grandiose",
      "splendid","precious","tragic","narrow","joyous","sharp","careless",
      "flat","resonant","bumpy","solid","strong","faulty","accidental"};
      return adjectives[d50]; //Returns a random adjective inside the array
   } //End randomAdjective

   public static String randomNoun() {
      int d50 = diceRoller(0, 49);
      String[] nouns = {"rise","campaign","secret","past","safety","pipe",
      "credit","rule","shelter","connection","farm","food","wealth","ratio",
      "failure","power","raw","system","square","money","sky","bear","black",
      "disease","pressure","final","weakness","data","beacon","equal",
      "penalty","keep","associate","control","elevator","company","concept",
      "disk","perception","professional","field","bone","natural","peak",
      "deep","future","extreme","night","knife","procedure"};
      return nouns[d50]; //Returns a random noun inside the array
   } //End randomNoun

   public static String randomDoor() {
      int d10 = diceRoller(0, 9);
      String[] doors = {"Gates","Doors","Openings","Portals","Apertures",
      "Entries","Hatches","Fissures","Rifts","Vents"};
      return doors[d10]; //Returns a random synonim of aperture
   } //End randomDoor

   public static String randomTrap() {
      int d4 = diceRoller(0, 3);
      String[] traps = {
         "Negative energy pillars and ethereal scythes hit you.",
         "Icy metal disks and precise ballistae shoot towards you.",
         "Forceful spears and energy-draining javelins get thrown towards you.",
         "Sonic swinging glaives and necrotic floortiles damage you." };
      return traps[d4]; //Same as other random Methods
   }

   /**************************
   All PLAYER Accessor Methods
   **************************/
   public static String getJob(Player p) { return p.job; }
   public static String getName(Player p) { return p.name; }
   public static int getXp(Player p) { return p.xp; }
   public static int getXpMax(Player p) { return p.xpMax; }
   public static int getLevel(Player p) { return p.level; }
   public static int getMoney(Player p) { return p.money; }
   public static int getRelics(Player p) { return p.relics; }
   public static int getHealth(Player p) { return p.health; }
   public static int getMaxHealth(Player p) { return p.maxHealth; }
   public static int getMana(Player p) { return p.mana; }
   public static int getMaxMana(Player p) { return p.maxMana; }
   public static int getAttack(Player p) { return p.attack; }
   public static int getDefense(Player p) { return p.defense; }
   public static int getDexterity(Player p) { return p.dexterity; }
   public static int getLuck(Player p) { return p.luck; }
   public static int getMagicDamage(Player p) { return p.magicDamage; }

   /***********************
   All ROOM Accesor Methods
   ***********************/
   public static int getRoomNumber(Room r) { return r.roomNumber; }
   public static String getRoomName(Room r) { return r.roomName; }
   public static String getRoomType(Room r) { return r.roomType; }
   public static int getDangerLevel(Room r) { return r.dangerLevel; }

   /*************************
   All ENEMY Accessor Methods
   *************************/
   public static String getEnemyName(Enemy e) { return e.name; }
   public static String getEnemyType(Enemy e) { return e.type; }
   public static int getEnemyHealth(Enemy e) { return e.health; }
   public static int getEnemyLevel(Enemy e) { return e.level; }
   public static int getEnemyAttack(Enemy e) { return e.attack; }
   public static int getEnemyDefense(Enemy e) { return e.defense; }

   /************************
   All MAGIC Accesor Methods
   ************************/
   public static String getSpellName(Magic m) { return m.spellName; }
   public static int getUnlockLevel(Magic m) { return m.unlockLevel; }
   public static int getManaCost(Magic m) { return m.manaCost; }
   public static double getDmgMultiplier(Magic m) { return m.dmgMultiplier; }

} //End class trappedCastle

class Player {
   String name;
   String job;
   int level;
   int xp;
   int xpMax;
   int health;
   int maxHealth;
   int mana;
   int maxMana;
   int attack;
   int defense;
   int luck;
   int dexterity;
   int relics;
   int money;
   int magicDamage;
} //End class Player

class Room {
   int roomNumber;
   String roomName;
   String roomType;
   int dangerLevel;
} //End class Room

class Enemy {
   String name;
   String type;
   int health;
   int level;
   int attack;
   int defense;
} //End class Enemy

class Weapon {
   String name;
   String rarity;
   int level;
   int attack;
} //End class Weapon

class Armor {
   String name;
   int rarity;
   int level;
   int defense;
} //End class Armor

class Magic {
   String spellName;
   int unlockLevel;
   int manaCost;
   double dmgMultiplier;
}
