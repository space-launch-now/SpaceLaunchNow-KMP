# Glossary for app text and store text

One list for the app text (`scripts/i18n/translate.py` sends this whole file to the model) and for
the store listings (Tasks#1215). Spec: [spec.md](spec.md), sections 7.2 and 8.1.

Target languages: German `de`, Spanish `es-ES`, French `fr`, Chinese (Simplified) `zh-CN`, Dutch `nl`,
Italian `it`, Polish `pl`, Portuguese (Brazil) `pt-BR`, Czech `cs`, Ukrainian `uk`.

## Rules for the translator

- Keep every placeholder (`%1$s`, `%2$d`, `%s`, `%d`) and every escape (`\n`) as in English.
- Write short, plain text for a mobile screen. Respect the length limit in the key comment.
- Use the informal "you" form where the language has one (de `du`, fr `tu`, es `tú`, it `tu`, nl `je`,
  pl, cs and uk `ty`, pt-BR `você`), unless a store rule needs the formal one.
- Terms marked **keep** stay in English. Terms marked **translate** use the word in the table; if a
  language has no fixed word, use the term spaceflight news in that language uses.
- A proper noun (agency, company, rocket, spacecraft, place, mission name) stays as written.

## Keep in English

| Term | Why |
|---|---|
| Space Launch Now | App name. Never translated. |
| The Space Devs | Organisation name. |
| SpaceX, NASA, ULA, Roscosmos, ISRO, Blue Origin, Rocket Lab, ESA, JAXA, CNSA | Agency and company names. |
| Falcon 9, Starship, Atlas V, Vulcan, Electron, New Glenn, Soyuz, Ariane 6 | Vehicle names. |
| NET | "No Earlier Than" on a launch date. Keep the letters; explain in the key comment text only where space allows. |
| T-minus, T-0 | Countdown notation. Keep the form `T-00:10:00`. |
| UTC | Time standard. |
| Go for Launch | The status name the API gives. Keep it as a label. |
| Premium | Name of the paid tier. |
| GitHub, Discord, Changelog | Names of services and a link label. |

## Translate

| English | Meaning in this app | Note |
|---|---|---|
| launch | A rocket launch event | Use the common spaceflight word, not "start" in the sense of "begin". |
| launch window | The time span in which a launch may happen | |
| scrub | A launch attempt that is called off before liftoff | Use the word for "cancel / call off" in the language, not "delete". |
| hold | A pause in the countdown | |
| liftoff | The moment the rocket leaves the pad | |
| payload | What the rocket carries (satellite, crew, cargo) | |
| booster | The first stage, or a strap-on stage | |
| first stage / second stage | Parts of the rocket | |
| crew | People flying on the mission | |
| mission | A flight with a goal | |
| pad | The launch pad | |
| landing | The booster or capsule coming back | |
| upcoming launches | Launches that have not happened | Not "future". |
| previous launches | Launches that have happened | |
| notification | A push message from the app | |
| subscribe | Turn on notifications for a launch or agency | Not a paid subscription. |
| widget | A home screen widget | Use the platform word. |
| countdown | The timer to liftoff | |

## Status words

| English | Meaning |
|---|---|
| Success | The launch reached orbit or its target. |
| Failure | The launch did not reach its target. |
| Partial failure | The rocket flew but the payload missed its orbit. |
| To Be Determined (TBD) | Date or time not set. |
| To Be Confirmed (TBC) | Date or time set but not final. |
